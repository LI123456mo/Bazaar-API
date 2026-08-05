package com.conel.market.repository.payment;

import com.conel.market.entity.payment.Payment;
import com.conel.market.entity.payment.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity with payment-specific queries.
 * 
 * Key features:
 * - Idempotency key lookup (UNIQUE)
 * - External reference queries (for reconciliation)
 * - Status-based queries (for payment lifecycle management)
 * - Timeout queries (for auto-refund logic)
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    /**
     * Find payment by idempotency key (for deduplication).
     * Used to check if identical payment request was already processed.
     * @param idempotencyKey unique identifier for request deduplication
     * @return Payment if found, empty if new request
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    /**
     * Find payment by external transaction reference (M-Pesa, Stripe, etc).
     * Used for reconciliation and duplicate detection.
     * @param externalRef transaction ID from payment gateway
     * @return Payment if matched
     */
    Optional<Payment> findByExternalTransactionRef(String externalRef);

    /**
     * Find payment by order ID.
     * @param orderId unique order identifier
     * @return Payment associated with order
     */
    Optional<Payment> findByOrderId(String orderId);

    /**
     * Find all payments with specific status.
     * Used for status-based reporting and batch operations.
     * @param status payment status to filter by
     * @param pageable pagination
     * @return page of payments with matching status
     */
    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    /**
     * Find all payments in PENDING state (waiting for user action or provider response).
     * Used by scheduler to check for timed-out payments.
     * @return list of pending payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' ORDER BY p.initiatedAt ASC")
    List<Payment> findAllPending();

    /**
     * Find payments that have exceeded timeout threshold (30+ minutes).
     * Used by scheduler to auto-refund stale payments.
     * @param thresholdTime cutoff timestamp
     * @return list of timed-out payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.initiatedAt < :threshold ORDER BY p.initiatedAt ASC")
    List<Payment> findTimedOutPayments(@Param("threshold") LocalDateTime thresholdTime);

    /**
     * Find payments ready for retry (next retry time <= now).
     * Used by scheduler to execute exponential backoff retries.
     * @param nowTime current timestamp
     * @return list of payments due for retry
     */
    @Query("SELECT p FROM Payment p WHERE p.status IN ('INITIATED', 'PENDING', 'FAILED') " +
           "AND p.nextRetryAt IS NOT NULL AND p.nextRetryAt <= :now " +
           "ORDER BY p.nextRetryAt ASC")
    List<Payment> findPaymentsReadyForRetry(@Param("now") LocalDateTime nowTime);

    /**
     * Find all payments created within date range.
     * Used for daily/monthly reconciliation reports.
     * @param startDate start of range (inclusive)
     * @param endDate end of range (inclusive)
     * @return payments in date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :start AND p.createdAt <= :end ORDER BY p.createdAt DESC")
    List<Payment> findPaymentsByDateRange(@Param("start") LocalDateTime startDate, @Param("end") LocalDateTime endDate);

    /**
     * Count payments by status.
     * Used for dashboard metrics and SLA monitoring.
     * @param status payment status
     * @return count of payments with this status
     */
    Long countByStatus(PaymentStatus status);

    /**
     * Find all payments not yet reconciled.
     * Used by reconciliation service.
     * @return list of unreconciled payments
     */
    @Query("SELECT p FROM Payment p WHERE p.reconciled = FALSE ORDER BY p.createdAt ASC")
    List<Payment> findUnreconciledPayments();

    /**
     * Find payments by user ID (for customer service queries).
     * @param userId user identifier
     * @param pageable pagination
     * @return customer's payments
     */
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    Page<Payment> findByUserId(@Param("userId") String userId, Pageable pageable);
}
