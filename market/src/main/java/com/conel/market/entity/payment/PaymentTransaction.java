package com.conel.market.entity.payment;

import com.conel.market.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Table(
        name = "payment_transactions",
        indexes = {
                @Index(name = "idx_payment_id", columnList = "payment_id"),
                @Index(name = "idx_external_txn_id", columnList = "external_transaction_id", unique = true),
                @Index(name = "idx_status", columnList = "status"),
                @Index(name = "idx_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PaymentTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Payment payment;

    @Column(length = 255)
    private String externalTransactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private TransactionStatus status;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(columnDefinition = "TEXT")
    private String requestHeaders;

    @Column(length = 10)
    private String httpMethod;

    @Column(length = 500)
    private String endpoint;

    @Column(length = 45)
    private String sourceIpAddress;

    @Column(length = 512)
    private String webhookSignature;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    private Boolean signatureVerified;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(length = 10)
    private String responseCode;

    private Long processingTimeMs;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    private Boolean processed;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer retryCount;

    private Instant nextRetryAt;

    private Instant processedAt;
}