package com.conel.market.exception.payment;

/**
 * Thrown when payment refund fails.
 * Moves to manual review queue.
 */
public class PaymentRefundException extends PaymentException {
    public PaymentRefundException(String paymentId, String reason) {
        super(String.format("Failed to refund payment %s: %s", paymentId, reason), 
              "REFUND_FAILED");
    }
}
