package com.conel.market.exception.payment;

/**
 * Thrown when concurrent payment update conflict occurs.
 * Optimistic locking violation: multiple processes updated payment simultaneously.
 */
public class PaymentConcurrencyException extends PaymentException {
    public PaymentConcurrencyException(String paymentId) {
        super(String.format("Payment %s was modified concurrently, please retry", paymentId), 
              "PAYMENT_CONCURRENCY_CONFLICT");
    }
}
