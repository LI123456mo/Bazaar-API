package com.conel.market.entity.payment;

import com.conel.market.entity.BaseEntity;
import com.conel.market.entity.order.Order;
import com.conel.market.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity representing a financial transaction.
 * 
 * KEY FEATURES:
 * - Optimistic locking with @Version to prevent concurrent update conflicts
 * - Idempotency key (UNIQUE constraint) to prevent duplicate charges
 * - Audit trail: createdAt, lastModifiedAt (inherited from BaseEntity)
 * - Payment status state machine enforcement
 * - Support for multiple payment methods (M-Pesa, Card, COD, Wallet)
 * - External gateway response tracking
 * - Comprehensive audit logging for regulatory compliance
 *
 * STATE MACHINE:
 *   INITIATED → PENDING → COMPLETED/FAILED/TIMEOUT → [REFUNDED]
 *
 * CONCURRENCY PATTERN:
 *   Uses @Version + idempotency key to handle:
 *   - Double-charging prevention
 *   - Webhook race conditions
 *   - Retry request idempotency
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_order_id", columnList = "order_id"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_idempotency_key", columnList = "idempotency_key", unique = true),
        @Index(name = "idx_external_ref", columnList = "external_transaction_ref"),
        @Index(name = "idx_created_at", columnList = "created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_idempotency_key", columnNames = {"idempotency_key"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {

    /**
     * OPTIMISTIC LOCKING: Prevents concurrent modifications.
     * JPA increments this on each update. If concurrent request tries to update
     * with stale version, OptimisticLockingFailureException is thrown.
     */
    @Version
    private Long version;

    /**
     * Reference to the Order this payment is for.
     * REQUIRED: Every payment must be linked to exactly one order.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    /**
     * Reference to the User who initiated the payment.
     * Cached separately for audit/query purposes.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /**
     * IDEMPOTENCY KEY: Unique identifier for deduplication.
     * Provided by client in request header: Idempotency-Key
     * Format: UUID or deterministic hash
     * 
     * CONSTRAINT: UNIQUE to prevent duplicate charges
     * If client retries with same key, returns cached result instead of charging twice
     */
    @Column(nullable = false, unique = true, length = 255)
    private String idempotencyKey;

    /**
     * Payment amount in local currency (KES for M-Pesa in Kenya).
     * Precision: 19,2 (supports up to 99,999,999.99)
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Currency code (ISO 4217).
     * E.g., "KES" for Kenyan Shilling, "USD" for US Dollar
     */
    @Column(nullable = false, length = 3)
    private String currency;

    /**
     * Payment method selected by customer.
     * Enumeration: M_PESA, CARD, COD, WALLET
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    /**
     * Current status in payment lifecycle.
     * Supports state machine validation: canTransitionTo()
     * Final states: COMPLETED, FAILED, REFUNDED, TIMEOUT, CANCELLED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /**
     * External transaction reference from payment gateway.
     * E.g., M-Pesa transaction ID, Stripe charge ID, etc.
     * Used for reconciliation and dispute resolution.
     */
    @Column(length = 255)
    private String externalTransactionRef;

    /**
     * Phone number for M-Pesa payments (Safaricom Daraja).
     * Format: International (254...) or National (0...)
     * Normalized during processing.
     */
    @Column(length = 20)
    private String phoneNumber;

    /**
     * Merchant request ID from Daraja API (M-Pesa).
     * Used for tracking payment requests in Safaricom system.
     */
    @Column(length = 255)
    private String merchantRequestId;

    /**
     * Checkout request ID from Daraja API (M-Pesa).
     * Used for tracking STK push request.
     */
    @Column(length = 255)
    private String checkoutRequestId;

    /**
     * Raw response/error details from payment gateway.
     * Stored as JSON for debugging and audit trail.
     */
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    /**
     * Error message if payment failed.
     * Human-readable description for customer.
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Timestamp when payment was initiated.
     * Used for timeout calculation (30-minute threshold).
     */
    @Column(nullable = false)
    private LocalDateTime initiatedAt;

    /**
     * Timestamp when payment status was last updated.
     * Useful for reconciliation and SLA monitoring.
     */
    private LocalDateTime statusUpdatedAt;

    /**
     * Timestamp when payment completed (if applicable).
     * NULL if not yet completed.
     */
    private LocalDateTime completedAt;

    /**
     * Number of retry attempts made for this payment.
     * Used for exponential backoff logic.
     */
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer retryCount;

    /**
     * Next scheduled retry timestamp.
     * Used by background job to retry failed payments.
     */
    private LocalDateTime nextRetryAt;

    /**
     * Whether this payment has been reconciled.
     * Set to TRUE after matching with bank statement.
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean reconciled;

    /**
     * Timestamp when payment was reconciled.
     * Used for audit trail and SLA metrics.
     */
    private LocalDateTime reconciledAt;

    /**
     * Validate status transition using state machine.
     * @param newStatus the target status
     * @throws IllegalStateException if transition is invalid
     */
    public void transitionTo(PaymentStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new IllegalStateException(
                String.format("Cannot transition from %s to %s", status, newStatus)
            );
        }
        this.status = newStatus;
        this.statusUpdatedAt = LocalDateTime.now();
        if (newStatus == PaymentStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    /**
     * Check if payment has exceeded timeout threshold (30 minutes).
     * @return true if initiated > 30 minutes ago and not yet completed
     */
    public boolean isTimedOut() {
        if (status.isFinal()) {
            return false;
        }
        return LocalDateTime.now().minusMinutes(30).isAfter(initiatedAt);
    }

    /**
     * Increment retry counter and calculate next retry time (exponential backoff).
     * @param baseDelaySeconds base delay in seconds (e.g., 1 second)
     */
    public void scheduleRetry(long baseDelaySeconds) {
        this.retryCount = (this.retryCount != null ? this.retryCount : 0) + 1;
        // Exponential backoff: 1s, 2s, 4s, 8s, 16s (max 5 attempts)
        long delaySeconds = baseDelaySeconds * (long) Math.pow(2, retryCount - 1);
        this.nextRetryAt = LocalDateTime.now().plusSeconds(delaySeconds);
    }
}
