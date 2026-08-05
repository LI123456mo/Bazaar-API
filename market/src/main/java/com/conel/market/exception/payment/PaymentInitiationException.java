package com.conel.market.exception.payment;

/**
 * Thrown when payment initiation fails.
 * E.g., M-Pesa API rejects request, invalid phone number, etc.
 */
public class PaymentInitiationException extends PaymentException {
    public PaymentInitiationException(String message) {
        super(message, "PAYMENT_INITIATION_FAILED");
    }

    public PaymentInitiationException(String message, Throwable cause) {
        super(message, "PAYMENT_INITIATION_FAILED", cause);
    }
}
