package com.conel.market.exception.payment;

/**
 * Thrown when webhook signature validation fails.
 * Security issue: potential forged webhook.
 */
public class InvalidWebhookSignatureException extends PaymentException {
    public InvalidWebhookSignatureException(String webhookId) {
        super(String.format("Webhook %s failed signature verification", webhookId), 
              "INVALID_WEBHOOK_SIGNATURE");
    }
}
