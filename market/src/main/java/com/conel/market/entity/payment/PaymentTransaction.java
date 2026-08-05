package com.conel.market.entity.payment;

import com.conel.market.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * PaymentTransaction records every webhook/callback received from payment gateway.
 * Used for:
 * - Idempotency: Prevents processing same webhook twice
 * - Replay protection: Stores exact payload for audit/dispute resolution
 * - Webhook deduplication: Safaricom may send duplicate webhooks
 *
 * PATTERN:
 * 1. Receive webhook with external transaction ID
 * 2. Check if PaymentTransaction already exists for this ID
 * 3. If exists: return cached result (idempotent)
 * 4. If not exists: process and create new record
 *
 * This ensures that duplicate webhooks (common in payment systems) don't
 * result in double-processing or state corruption.
 */
@Entity
@Table(
    name = "payment_transactions",
    indexes = {
        @Index(name = "idx_payment_id", columnList = "payment_id"),
        @Index(name = "idx_external_txn_id", columnList = "external_transaction_id", unique = true),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_created_at", columnList = "created_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_external_txn_id", columnNames = {"external_transaction_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PaymentTransaction extends BaseEntity {

    /**
     * Reference to the Payment this transaction is for.
     * Multiple transactions can exist for single payment (retries, reversals)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Payment payment;

    /**
     * External transaction ID from payment gateway.
     * E.g., M-Pesa MPesaReceiptNumber
     * UNIQUE: Guarantees no duplicate processing
     */
    @Column(nullable = false, unique = true, length = 255)
    private String externalTransactionId;

    /**
     * Type of transaction: INITIATED, CALLBACK, WEBHOOK, WEBHOOK_RETRY
     * Used to track flow of transaction through system
     */
    @Column(nullable = false, length = 50)
    private String transactionType;

    /**
     * Raw webhook/callback payload from payment gateway.
     * Stored as JSON for complete audit trail.
     * Important for disputes and compliance.
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    /**
     * Processing status: RECEIVED, PROCESSED, FAILED, DUPLICATE
     * - RECEIVED: Webhook arrived but not yet processed
     * - PROCESSED: Successfully processed
     * - FAILED: Processing failed (will retry)
     * - DUPLICATE: Same webhook received again (idempotent, ignored)
     */
    @Column(nullable = false, length = 50)
    private String status;

    /**
     * Error message if processing failed.
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * HTTP response code from the payment gateway (if applicable).
     * E.g., 200, 400, 500
     */
    @Column(length = 10)
    private String responseCode;

    /**
     * IP address of the payment gateway (for security verification).
     * Useful for validating webhook source.
     */
    @Column(length = 45)
    private String sourceIpAddress;

    /**
     * Signature received in webhook header.
     * Used to verify authenticity of webhook (SHA-256).
     */
    @Column(length = 512)
    private String webhookSignature;

    /**
     * Whether signature was successfully verified.
     * CRITICAL for security: Only process webhooks with valid signatures.
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean signatureVerified;

    /**
     * Number of times this transaction has been retried.
     * Useful for troubleshooting persistent failures.
     */
    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer retryCount;
}
