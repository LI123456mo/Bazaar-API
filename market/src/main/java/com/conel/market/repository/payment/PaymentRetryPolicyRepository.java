package com.conel.market.repository.payment;

import com.conel.market.entity.payment.PaymentRetryPolicy;
import com.conel.market.entity.payment.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentRetryPolicy entity.
 * Manages configurable retry strategies per payment method.
 */
@Repository
public interface PaymentRetryPolicyRepository extends JpaRepository<PaymentRetryPolicy, String> {

    /**
     * Find active retry policy for specific payment method.
     * @param paymentMethod payment method (M_PESA, CARD, COD, WALLET)
     * @return active policy if exists
     */
    @Query("SELECT p FROM PaymentRetryPolicy p WHERE p.paymentMethod = :method AND p.active = TRUE")
    Optional<PaymentRetryPolicy> findActiveByPaymentMethod(@Param("method") PaymentMethod paymentMethod);

    /**
     * Find default active policy (applies to all methods).
     * Used when no method-specific policy exists.
     * @return default policy
     */
    @Query("SELECT p FROM PaymentRetryPolicy p WHERE p.paymentMethod IS NULL AND p.active = TRUE")
    Optional<PaymentRetryPolicy> findDefaultPolicy();

    /**
     * Find all active retry policies.
     * @return list of active policies
     */
    @Query("SELECT p FROM PaymentRetryPolicy p WHERE p.active = TRUE ORDER BY p.paymentMethod ASC")
    List<PaymentRetryPolicy> findAllActive();

    /**
     * Find policy by name (for configuration/debugging).
     * @param name policy name
     * @return matching policy
     */
    Optional<PaymentRetryPolicy> findByName(String name);
}
