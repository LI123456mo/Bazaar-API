package com.conel.market.exception.payment;

/**
 * Thrown when invalid status transition is attempted.
 * E.g., COMPLETED -> PENDING (violates state machine)
 */
public class InvalidPaymentStatusTransitionException extends PaymentException {
    public InvalidPaymentStatusTransitionException(String fromStatus, String toStatus) {
        super(String.format("Cannot transition payment from %s to %s", fromStatus, toStatus), 
              "INVALID_STATUS_TRANSITION");
    }
}
