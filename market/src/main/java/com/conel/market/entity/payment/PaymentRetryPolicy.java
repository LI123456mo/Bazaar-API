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
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString
public class PaymentRetryPolicy extends BaseEntity {


    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private PaymentMethod paymentMethod;

    /**
     * Human-readable name for this policy.
     * E.g., "Standard M-Pesa Retry", "Credit Card Immediate Retry"
     */
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

    /**
     * Maximum total time allowed for retries (seconds).
     * If retry time exceeds this, stop retrying regardless of attempt count.
     * Default: 300 seconds (5 minutes)
     * Used to prevent indefinite retry loops.
     */
    @Column(nullable = false, columnDefinition = "INT DEFAULT 300")
    private Integer maxRetryDurationSeconds;

    /**
     * Whether this policy is currently active.
     * Inactive policies are ignored during retry scheduling.
     */
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE", nullable = false)
    private Boolean active;

    /**
     * Calculate delay for attempt N using exponential backoff formula.
     * delay = baseDelaySeconds * (backoffMultiplier ^ attemptNumber)
     *
     * @param attemptNumber (1-indexed, so 2nd attempt = attemptNumber 2)
     * @return delay in seconds
     */
    public long calculateDelay(int attemptNumber) {
        return (long) (baseDelaySeconds * Math.pow(backoffMultiplier, attemptNumber - 1));
    }

    /**
     * Check if retry attempt should proceed based on this policy.
     *
     * @param currentRetryCount number of retries already attempted
     * @param totalElapsedSeconds seconds elapsed since first attempt
     * @return true if retry should be scheduled, false if max attempts/duration exceeded
     */
    public boolean shouldRetry(int currentRetryCount, long totalElapsedSeconds) {
        if (currentRetryCount >= maxRetries) {
            return false; // Max retries exceeded
        }
        if (totalElapsedSeconds >= maxRetryDurationSeconds) {
            return false; // Max duration exceeded
        }
        return true;
    }
}
