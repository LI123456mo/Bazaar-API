package com.conel.market.repository.payment;

import com.conel.market.entity.payment.Payment;
import com.conel.market.entity.payment.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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


    Optional<Payment> findByIdempotencyKey(String idempotencyKey);


    Optional<Payment> findByExternalTransactionRef(String externalRef);

    Optional<Payment> findByCheckoutRequestId(String checkoutRequestId);

    List<Payment> findAllByOrderId(String orderId);


    @Query("SELECT p FROM Payment p WHERE p.order.id = :orderId AND p.status = 'COMPLETED'")
    Optional<Payment> findCompletedPaymentByOrderId(@Param("orderId") String orderId);


    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);


    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' ORDER BY p.initiatedAt ASC")
    List<Payment> findAllPending();

    /**
     * Find payments that have exceeded timeout threshold (30+ minutes).
     * Used by scheduler to auto-refund stale payments.
     * @param thresholdTime cutoff timestamp
     * @return list of timed-out payments
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.initiatedAt < :threshold ORDER BY p.initiatedAt ASC")
    List<Payment> findTimedOutPayments(@Param("threshold") Instant thresholdTime);

    /**
     * Find payments ready for retry (next retry time <= now).
     * Used by scheduler to execute exponential backoff retries.
     * @param nowTime current timestamp
     * @return list of payments due for retry
     */
    @Query("SELECT p FROM Payment p WHERE p.status IN ('INITIATED', 'PENDING', 'FAILED') " +
           "AND p.nextRetryAt IS NOT NULL AND p.nextRetryAt <= :now " +
           "ORDER BY p.nextRetryAt ASC")
    List<Payment> findPaymentsReadyForRetry(@Param("now") Instant nowTime);

    /**
     * Find all payments created within date range.
     * Used for daily/monthly reconciliation reports.
     * @param startDate start of range (inclusive)
     * @param endDate end of range (inclusive)
     * @return payments in date range
     */
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :start AND p.createdAt <= :end ORDER BY p.createdAt DESC")
    List<Payment> findPaymentsByDateRange(@Param("start") Instant startDate, @Param("end") Instant endDate);

    /**
     * Count payments by status.
     * Used for dashboard metrics and SLA monitoring.
     * @param status payment status
     * @return count of payments with this status
     */
    Long countByStatus(PaymentStatus status);


    @Query("SELECT p FROM Payment p WHERE p.reconciled = FALSE ORDER BY p.createdAt ASC")
    List<Payment> findUnreconciledPayments();


    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    Page<Payment> findByUserId(@Param("userId") String userId, Pageable pageable);
}
