package com.conel.market.exception.payment;

/**
 * Thrown when duplicate payment submission is detected.
 * Same idempotency key already processed.
 */
public class DuplicatePaymentException extends PaymentException {
    public DuplicatePaymentException(String idempotencyKey) {
        super(String.format("Payment with idempotency key %s already exists", idempotencyKey), 
              "DUPLICATE_PAYMENT");
    }
}
