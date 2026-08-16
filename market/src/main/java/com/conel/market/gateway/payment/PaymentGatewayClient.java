package com.conel.market.gateway.payment;

import com.conel.market.dto.payment.PaymentInitiationRequest;
import com.conel.market.entity.payment.PaymentMethod;

import java.math.BigDecimal;

public interface PaymentGatewayClient {

    GatewayInitiationResult initiate(PaymentInitiationRequest request, BigDecimal amount, String idempotencyKey);

    boolean verifySignature(String rawPayload, String signatureHeader);

    PaymentMethod supportedMethod();

    record GatewayInitiationResult(
            String merchantRequestId,
            String checkoutRequestId,
            boolean accepted,
            String rawResponse
    ) {}
}