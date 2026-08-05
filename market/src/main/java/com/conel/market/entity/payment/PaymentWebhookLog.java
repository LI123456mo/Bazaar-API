package com.conel.market.entity.payment;

import com.conel.market.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * PaymentWebhookLog provides immutable audit trail for all webhook activities.
 * Required for PCI DSS compliance and regulatory audits.
 * 
 * Captured data:
 * - Incoming webhook payload (raw JSON)
 * - Processing status and result
 * - Timestamps for SLA monitoring
 * - Signature verification outcome
 * - Any errors during processing
 *
 * USE CASE: Regulatory compliance
 * - Auditor can trace every webhook received
 * - Verify all payments were processed correctly
 * - Prove that failed webhooks were retried
 * - Document any payment discrepancies
 */
@Entity
@Table(
    name = "payment_webhook_logs",
    indexes = {
        @Index(name = "idx_payment_id", columnList = "payment_id"),
        @Index(name = "idx_webhook_id", columnList = "webhook_id"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_processed", columnList = "processed"),
        @Index(name = "idx_created_at", columnList = "created_at")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PaymentWebhookLog extends BaseEntity {

    /**
     * Reference to the Payment this webhook is for.
     * May be NULL if payment ID cannot be extracted (malformed webhook).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Payment payment;

    /**
     * Unique webhook ID from payment gateway (if provided).
     * E.g., Safaricom webhook ID for deduplication
     */
    @Column(length = 255)
    private String webhookId;

    /**
     * Raw HTTP request body received from payment gateway.
     * Stored exactly as received for audit purposes.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String requestBody;

    /**
     * HTTP headers from incoming webhook request.
     * Stored as JSON for signature verification audit.
     */
    @Column(columnDefinition = "TEXT")
    private String requestHeaders;

    /**
     * HTTP method (GET, POST, PUT, etc.)
     * Usually POST for payment webhooks.
     */
    @Column(length = 10)
    private String httpMethod;

    /**
     * Webhook endpoint that received the request.
     * E.g., /api/payments/webhook/mpesa
     */
    @Column(length = 500)
    private String endpoint;

    /**
     * IP address of webhook source (payment gateway).
     * Useful for whitelisting and security verification.
     */
    @Column(length = 45)
    private String sourceIp;

    /**
     * Processing status: RECEIVED, VALIDATED, PROCESSED, FAILED, DISCARDED
     * - RECEIVED: Initial state
     * - VALIDATED: Signature verified successfully
     * - PROCESSED: Payment state updated based on webhook
     * - FAILED: Processing encountered error (will retry)
     * - DISCARDED: Webhook rejected (malformed, old, etc.)
     */
    @Column(nullable = false, length = 50)
    private String status;

    /**
     * Whether signature was successfully verified.
     * CRITICAL: Only process webhooks where this is TRUE.
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    private Boolean signatureValid;

    /**
     * Processing result/error message.
     * Human-readable description of what happened.
     */
    @Column(columnDefinition = "TEXT")
    private String processingResult;

    /**
     * HTTP response code sent back to payment gateway.
     * 200: Webhook processed successfully
     * 202: Accepted, will process asynchronously
     * 400: Bad request (signature invalid)
     * 500: Server error (retry later)
     */
    @Column(length = 10)
    private String responseCode;

    /**
     * Time taken to process webhook (milliseconds).
     * Used for performance monitoring and SLA tracking.
     */
    @Column
    private Long processingTimeMs;

    /**
     * Whether webhook was processed successfully.
     * FALSE if status = FAILED or DISCARDED
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    private Boolean processed;

    /**
     * Retry count for this webhook.
     * Incremented on each failed attempt.
     */
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer retryCount;

    /**
     * Next scheduled retry timestamp (if processing failed).
     * NULL if not scheduled for retry.
     */
    @Column
    private java.time.LocalDateTime nextRetryAt;

    /**
     * Timestamp when webhook was first received.
     * (Inherited createdAt from BaseEntity)
     * Used for SLA monitoring (e.g., webhooks should process < 5 seconds)
     */

    /**
     * Timestamp when webhook processing completed.
     * Null if still processing.
     */
    @Column
    private java.time.LocalDateTime processedAt;
}
