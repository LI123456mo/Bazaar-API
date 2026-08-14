package com.conel.market.entity.payment;

import com.conel.market.entity.BaseEntity;
import com.conel.market.entity.order.Order;
import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

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
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Payment extends BaseEntity {

    private static final long TIMEOUT_MINUTES = 30;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(nullable = false, length = 255)
    private String idempotencyKey;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(length = 255)
    private String externalTransactionRef;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 255)
    private String merchantRequestId;

    @Column(length = 255)
    private String checkoutRequestId;

    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(nullable = false)
    private Instant initiatedAt;

    private LocalDateTime statusUpdatedAt;

    private LocalDateTime completedAt;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer retryCount;

    private LocalDateTime nextRetryAt;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean reconciled;

    private LocalDateTime reconciledAt;

    public void transitionTo(PaymentStatus newStatus) {
        if (!status.canTransitionTo(newStatus)) {
            throw new BusinessException(
                    ErrorCode.INVALID_PAYMENT_STATE_TRANSITION,
                    String.format("Cannot transition payment from %s to %s", status, newStatus)
            );
        }
        this.status = newStatus;
        this.statusUpdatedAt = LocalDateTime.now();
        if (newStatus == PaymentStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public boolean isTimedOut() {
        if (status.isFinal()) return false;
        return Instant.now().isAfter(initiatedAt.plusSeconds(TIMEOUT_MINUTES * 60));
    }

    public void scheduleRetry(long baseDelaySeconds) {
        this.retryCount = (this.retryCount != null ? this.retryCount : 0) + 1;
        long delaySeconds = baseDelaySeconds * (long) Math.pow(2, retryCount - 1);
        this.nextRetryAt = LocalDateTime.now().plusSeconds(delaySeconds);
    }
}