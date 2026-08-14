package com.conel.market.entity.payment;

import com.conel.market.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "payment_retry_policies",
        indexes = {
                @Index(name = "idx_payment_method", columnList = "payment_method"),
                @Index(name = "idx_active", columnList = "active")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString
public class PaymentRetryPolicy extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer baseDelaySeconds;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 2")
    private Integer backoffMultiplier;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 5")
    private Integer maxRetries;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 300")
    private Integer maxRetryDurationSeconds;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", nullable = false)
    private Boolean active;

    public long calculateDelay(int attemptNumber) {
        return (long) (baseDelaySeconds * Math.pow(backoffMultiplier, attemptNumber - 1));
    }

    public boolean shouldRetry(int currentRetryCount, long totalElapsedSeconds) {
        if (currentRetryCount >= maxRetries) return false;
        if (totalElapsedSeconds >= maxRetryDurationSeconds) return false;
        return true;
    }
}