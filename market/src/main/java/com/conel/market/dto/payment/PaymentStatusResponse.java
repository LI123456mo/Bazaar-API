package com.conel.market.dto.payment;

import com.conel.market.entity.payment.PaymentMethod;
import com.conel.market.entity.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detailed payment status response.
 * Returned when client queries payment status.
 * 
 * Contains full payment lifecycle information:
 * - Current status and any errors
 * - Amount, method, external reference
 * - Timestamps for all state transitions
 * - Retry information if applicable
 * 
 * Used by client to:
 * - Check if payment succeeded
 * - Display retry status
 * - Show error message to user
 * - Track payment processing time
 */
public record PaymentStatusResponse(
    String paymentId,
    String orderId,
    PaymentStatus status,
    PaymentMethod paymentMethod,
    BigDecimal amount,
    String currency,
    String externalTransactionRef,
    LocalDateTime initiatedAt,
    LocalDateTime statusUpdatedAt,
    LocalDateTime completedAt,
    String errorMessage,
    Integer retryCount,
    LocalDateTime nextRetryAt,
    Boolean reconciled,
    String message
) {
    /**
     * Factory method to create response from Payment entity.
     */
    public static PaymentStatusResponse from(com.conel.market.entity.payment.Payment payment) {
        return new PaymentStatusResponse(
            payment.getId(),
            payment.getOrder().getId(),
            payment.getStatus(),
            payment.getPaymentMethod(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getExternalTransactionRef(),
            payment.getInitiatedAt(),
            payment.getStatusUpdatedAt(),
            payment.getCompletedAt(),
            payment.getErrorMessage(),
            payment.getRetryCount(),
            payment.getNextRetryAt(),
            payment.getReconciled(),
            payment.getStatus().getDescription()
        );
    }
}
