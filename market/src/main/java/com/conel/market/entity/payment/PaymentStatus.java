package com.conel.market.entity.payment;

/**
 * Payment lifecycle states.
 * State machine: INITIATED → PENDING → (COMPLETED | FAILED) → [optional: REFUNDED]
 * TIMEOUT: Payment not completed within threshold (30 minutes)
 */
public enum PaymentStatus {
    INITIATED("Payment initiated, awaiting provider response"),
    PENDING("Payment pending, waiting for user action (e.g., M-Pesa STK prompt)"),
    COMPLETED("Payment successfully completed and verified"),
    FAILED("Payment failed, transaction rejected"),
    REFUNDED("Payment refunded to user"),
    TIMEOUT("Payment timed out after 30 minutes of inactivity"),
    CANCELLED("Payment cancelled by user or system");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == REFUNDED || this == TIMEOUT || this == CANCELLED;
    }

    public boolean canTransitionTo(PaymentStatus targetStatus) {
        return switch (this) {
            case INITIATED -> targetStatus == PENDING || targetStatus == FAILED || targetStatus == CANCELLED;
            case PENDING -> targetStatus == COMPLETED || targetStatus == FAILED || targetStatus == TIMEOUT || targetStatus == CANCELLED;
            case COMPLETED -> targetStatus == REFUNDED;
            case FAILED, REFUNDED, TIMEOUT, CANCELLED -> false;
        };
    }
}
