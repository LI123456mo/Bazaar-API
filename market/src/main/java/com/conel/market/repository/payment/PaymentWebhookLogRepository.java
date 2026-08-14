package com.conel.market.repository.payment;

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
 * Repository for PaymentWebhookLog entity.
 * Immutable audit trail of all webhook activity for compliance.
 */
@Repository
public interface PaymentWebhookLogRepository extends JpaRepository<PaymentWebhookLog, String> {

    /**
     * Find webhook log by webhook ID (provided by payment gateway).
     * @param webhookId webhook identifier from payment gateway
     * @return webhook log if exists
     */
    Optional<PaymentWebhookLog> findByWebhookId(String webhookId);

    /**
     * Find all webhook logs for a specific payment.
     * Useful for debugging payment issues.
     * @param paymentId payment identifier
     * @return list of webhooks for this payment
     */
    @Query("SELECT w FROM PaymentWebhookLog w WHERE w.payment.id = :paymentId ORDER BY w.createdAt DESC")
    List<PaymentWebhookLog> findByPaymentId(@Param("paymentId") String paymentId);

    /**
     * Find webhook logs with specific processing status.
     * @param status RECEIVED, VALIDATED, PROCESSED, FAILED, DISCARDED
     * @param pageable pagination
     * @return webhooks with matching status
     */
    Page<PaymentWebhookLog> findByStatus(String status, Pageable pageable);

    /**
     * Find failed webhook logs (for retry handling).
     * @return list of failed webhooks
     */
    @Query("SELECT w FROM PaymentWebhookLog w WHERE w.status = 'FAILED' ORDER BY w.createdAt ASC")
    List<PaymentWebhookLog> findFailedWebhooks();

    /**
     * Find webhooks with invalid signatures (security issue).
     * @return webhooks that failed signature verification
     */
    @Query("SELECT w FROM PaymentWebhookLog w WHERE w.signatureValid = FALSE ORDER BY w.createdAt DESC")
    List<PaymentWebhookLog> findInvalidSignatureWebhooks();

    /**
     * Find webhooks received from specific IP (for auditing/blocking).
     * @param ipAddress source IP address
     * @param pageable pagination
     * @return webhooks from this IP
     */
    Page<PaymentWebhookLog> findBySourceIp(String ipAddress, Pageable pageable);

    /**
     * Find all unprocessed webhooks (scheduled for retry).
     * @return webhooks with status = FAILED or RECEIVED
     */
    @Query("SELECT w FROM PaymentWebhookLog w WHERE w.status IN ('FAILED', 'RECEIVED') AND w.nextRetryAt <= :now ORDER BY w.createdAt ASC")
    List<PaymentWebhookLog> findWebhooksReadyForRetry(@Param("now") LocalDateTime now);

    /**
     * Find webhooks received within date range (for audit reports).
     * @param startTime start of range
     * @param endTime end of range
     * @return webhooks in date range
     */
    @Query("SELECT w FROM PaymentWebhookLog w WHERE w.createdAt >= :start AND w.createdAt <= :end ORDER BY w.createdAt DESC")
    List<PaymentWebhookLog> findByDateRange(@Param("start") LocalDateTime startTime, @Param("end") LocalDateTime endTime);

    /**
     * Count webhook logs by status (for dashboard metrics).
     * @param status webhook processing status
     * @return count of webhooks with this status
     */
    Long countByStatus(String status);
}
