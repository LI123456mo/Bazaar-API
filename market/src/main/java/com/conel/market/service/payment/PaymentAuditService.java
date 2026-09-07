package com.conel.market.service.payment;

import com.conel.market.entity.payment.Payment;
import com.conel.market.entity.payment.PaymentRetryPolicy;
import com.conel.market.entity.payment.PaymentStatus;
import com.conel.market.repository.payment.PaymentRepository;
import com.conel.market.repository.payment.PaymentRetryPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentAuditService {

    private final PaymentRepository paymentRepository;
    private final PaymentRetryPolicyRepository paymentRetryPolicyRepository;

    /**
     * Commits independently of the caller's transaction (REQUIRES_NEW), so a FAILED
     * record survives even when the caller's own transaction rolls back after this
     * returns — e.g. when the caller re-throws an exception right after calling this.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Payment payment, String errorMessage) {
        payment.setErrorMessage(errorMessage);
        payment.transitionTo(PaymentStatus.FAILED);
        Optional<PaymentRetryPolicy> policy = paymentRetryPolicyRepository.findActiveByPaymentMethod(payment.getPaymentMethod());
        policy.ifPresent(p -> payment.scheduleRetry(p.getBaseDelaySeconds()));
        paymentRepository.save(payment);
    }

    public void scheduleFirstRetryIfEligible(Payment payment) {
        paymentRetryPolicyRepository.findActiveByPaymentMethod(payment.getPaymentMethod())
                .ifPresent(p -> payment.scheduleRetry(p.getBaseDelaySeconds()));
        //Only care about the "found" case, don't need to react to "not found" → ifPresent(...)
    }
}