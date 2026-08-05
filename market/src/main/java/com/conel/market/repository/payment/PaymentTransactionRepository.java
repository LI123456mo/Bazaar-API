package com.conel.market.repository.payment;

import com.conel.market.entity.payment.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentTransaction entity.
 * Handles webhook deduplication and idempotency tracking.
 *
 * Key pattern:
 * 1. Webhook received with externalTransactionId
 * 2. Query: findByExternalTransactionId(id)
 * 3. If found: return cached result (idempotent)
 * 4. If not found: process and save new transaction
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, String> {

    /**
     * Find transaction by external transaction ID (webhook deduplication).
     * UNIQUE constraint ensures only one record per external ID.
     * @param externalTransactionId ID from payment gateway
     * @return existing transaction if already processed
     */
    Optional<PaymentTransaction> findByExternalTransactionId(String externalTransactionId);

    /**
     * Find all transactions for a payment (multiple webhooks/callbacks possible).
     * @param paymentId payment identifier
     * @return list of all transactions for this payment
     */
    @Query("SELECT t FROM PaymentTransaction t WHERE t.payment.id = :paymentId ORDER BY t.createdAt DESC")
    List<PaymentTransaction> findByPaymentId(@Param("paymentId") String paymentId);

    /**
     * Find failed transactions ready for retry.
     * @return list of transactions with status = 'FAILED'
     */
    @Query("SELECT t FROM PaymentTransaction t WHERE t.status = 'FAILED' ORDER BY t.createdAt ASC")
    List<PaymentTransaction> findFailedTransactions();

    /**
     * Find duplicate webhooks (same external ID, multiple entries).
     * Useful for debugging webhook deduplication issues.
     * @param externalTransactionId external ID
     * @return all transactions with this external ID
     */
    @Query("SELECT t FROM PaymentTransaction t WHERE t.externalTransactionId = :externalId ORDER BY t.createdAt DESC")
    List<PaymentTransaction> findDuplicatesByExternalId(@Param("externalId") String externalTransactionId);
}
