package com.conel.market.gateway.payment;

import com.conel.market.dto.payment.PaymentInitiationRequest;
import com.conel.market.entity.payment.PaymentMethod;
import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Daraja doesn't sign callbacks with a header. Authenticity is
 * established instead by (a) restricting callbacks to Safaricom's published IP ranges
 * and (b) PaymentService matching the callback's CheckoutRequestID against a Payment
 * we actually created and are still waiting on.
 */
@Slf4j
@Component
public class MpesaGatewayClient implements PaymentGatewayClient {

    private static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    //TODO -> to be checked on prod daraja docs
    private static final Set<String> SAFARICOM_CALLBACK_IPS = Set.of(
            "196.201.214.200", "196.201.214.206", "196.201.213.114",
            "196.201.214.207", "196.201.214.208", "196.201.213.44",
            "196.201.212.127", "196.201.212.128", "196.201.212.129",
            "196.201.212.132", "196.201.212.136", "196.201.212.138"
    );

    private final RestClient restClient;
    private final AtomicReference<CachedToken> tokenCache = new AtomicReference<>();

    @Value("${mpesa.consumer-key}")
    private String consumerKey;

    @Value("${mpesa.consumer-secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String shortcode;

    @Value("${mpesa.passkey}")
    private String passkey;

    @Value("${mpesa.callback-url}")
    private String callbackUrl;

    @Value("${mpesa.base-url}")
    private String baseUrl;

    public MpesaGatewayClient() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(15000);

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.M_PESA;
    }

    /**
     * Returns a cached access token if it still has more than 60s of validity left,
     * otherwise fetches and caches a new one. Synchronized so concurrent requests
     * don't all trigger a fresh OAuth call at once.
     */
    private synchronized String getAccessToken() {
        CachedToken cached = tokenCache.get();
        if (cached != null && cached.expiresAt().isAfter(Instant.now().plusSeconds(60))) {
            return cached.token();
        }

        String credentials = Base64.getEncoder()
                .encodeToString((consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));

        try {
            JsonNode response = restClient.get()
                    .uri(baseUrl + "/oauth/v1/generate?grant_type=client_credentials")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || response.path("access_token").isMissingNode()) {
                throw new IllegalStateException("Daraja auth response missing access_token");
            }

            String token = response.path("access_token").asText();
            long expiresInSeconds = response.path("expires_in").asLong(3600);

            tokenCache.set(new CachedToken(token, Instant.now().plusSeconds(expiresInSeconds)));
            return token;
        } catch (Exception e) {
            log.error("Failed to obtain M-Pesa access token", e);
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE, "Could not authenticate with M-Pesa");
        }
    }

    @Override
    public GatewayInitiationResult initiate(PaymentInitiationRequest request, BigDecimal amount, String idempotencyKey) {
        String accessToken = getAccessToken();

        String timestamp = LocalDateTime.now(NAIROBI).format(TIMESTAMP_FORMAT);
        String password = Base64.getEncoder().encodeToString(
                (shortcode + passkey + timestamp).getBytes(StandardCharsets.UTF_8)
        );

        Map<String, Object> body = new HashMap<>();
        body.put("BusinessShortCode", shortcode);
        body.put("Password", password);
        body.put("Timestamp", timestamp);
        body.put("TransactionType", "CustomerPayBillOnline");
        body.put("Amount", amount.setScale(0, RoundingMode.HALF_UP));
        body.put("PartyA", request.phoneNumber());
        body.put("PartyB", shortcode);
        body.put("PhoneNumber", request.phoneNumber());
        body.put("CallBackURL", callbackUrl);
        body.put("AccountReference", request.orderId());
        body.put("TransactionDesc", "Payment for order " + request.orderId());

        try {
            JsonNode response = restClient.post()
                    .uri(baseUrl + "/mpesa/stkpush/v1/processrequest")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null) {
                throw new IllegalStateException("Empty response body from Daraja STK push endpoint");
            }

            String merchantRequestId = response.path("MerchantRequestID").asText(null);
            String checkoutRequestId = response.path("CheckoutRequestID").asText(null);
            String responseCode = response.path("ResponseCode").asText("");

            return new GatewayInitiationResult(
                    merchantRequestId,
                    checkoutRequestId,
                    "0".equals(responseCode),
                    response.toString()
            );
        } catch (Exception e) {
            log.error("M-Pesa STK push failed for order {}", request.orderId(), e);
            throw new BusinessException(ErrorCode.PAYMENT_INITIATION_FAILED, "Failed to initiate M-Pesa payment. Please try again.");
        }
    }

    /**
     * Real verification for M-Pesa: check the callback came from a known Safaricom IP.
     * Matching against a Payment we actually created happens separately in PaymentService,
     * since that needs PaymentRepository, which gateway clients deliberately don't hold.
     */
    @Override
    public boolean verifySignature(String rawPayload, String signatureHeader, String sourceIp) {
        if (sourceIp == null || !SAFARICOM_CALLBACK_IPS.contains(sourceIp)) {
            log.warn("Rejected M-Pesa callback from untrusted source IP: {}", sourceIp);
            return false;
        }
        return true;
    }

    private record CachedToken(String token, Instant expiresAt) {
    }
}