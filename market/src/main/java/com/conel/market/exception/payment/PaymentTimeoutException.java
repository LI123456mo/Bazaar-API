package com.conel.market.exception.payment;

/**
 * Thrown when payment timeout threshold is exceeded (30 minutes).
 * Payment moves to TIMEOUT status, order may be cancelled.
 */
public class PaymentTimeoutException extends PaymentException {
    public PaymentTimeoutException(String paymentId) {
        super(String.format("Payment %s timed out after 30 minutes", paymentId), "PAYMENT_TIMEOUT");
    }
}
