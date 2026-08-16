package com.conel.market.gateway.payment;

import com.conel.market.dto.payment.PaymentInitiationRequest;
import com.conel.market.entity.payment.PaymentMethod;
import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Component
public class MpesaGatewayClient implements PaymentGatewayClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

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

    public MpesaGatewayClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.M_PESA;
    }

    private String getAccessToken() {
        String credentials = Base64.getEncoder()
                .encodeToString((consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));

        try {
            JsonNode response = restClient.get()
                    .uri(baseUrl + "/oauth/v1/generate?grant_type=client_credentials")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                    .retrieve()
                    .body(JsonNode.class);

            return response.get("access_token").asText();
        } catch (Exception e) {
            log.error("Failed to obtain M-Pesa access token", e);
            throw new BusinessException(ErrorCode.PAYMENT_GATEWAY_UNAVAILABLE, "Could not authenticate with M-Pesa");
        }
    }

    @Override
    public GatewayInitiationResult initiate(PaymentInitiationRequest request, BigDecimal amount, String idempotencyKey) {
        String accessToken = getAccessToken();

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String password = Base64.getEncoder().encodeToString(
                (shortcode + passkey + timestamp).getBytes(StandardCharsets.UTF_8)
        );

        Map<String, Object> body = Map.of(
                "BusinessShortCode", shortcode,
                "Password", password,
                "Timestamp", timestamp,
                "TransactionType", "CustomerPayBillOnline",
                "Amount", amount.setScale(0, java.math.RoundingMode.HALF_UP),
                "PartyA", request.phoneNumber(),
                "PartyB", shortcode,
                "PhoneNumber", request.phoneNumber(),
                "CallBackURL", callbackUrl,
                "AccountReference", request.orderId(),
                "TransactionDesc", "Payment for order " + request.orderId()
        );
        try {
            JsonNode response = restClient.post()
                    .uri(baseUrl + "/mpesa/stkpush/v1/processrequest")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

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
            throw new BusinessException(ErrorCode.PAYMENT_INITIATION_FAILED, "Failed to initiate M-Pesa payment: " + e.getMessage());
        }
    }

    @Override
    public boolean verifySignature(String rawPayload, String signatureHeader) {
        // Daraja does not sign webhooks with a header-based signature like Stripe does.
        // Real protection here is validating the source IP and/or matching
        // MerchantRequestID/CheckoutRequestID against a PaymentTransaction we actually created.
        // Placeholder until we design that check together in PaymentService.
        return true;
    }
}