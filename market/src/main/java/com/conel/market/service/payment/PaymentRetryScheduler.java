package com.conel.market.service.payment;

import com.conel.market.dto.payment.PaymentInitiationRequest;
import com.conel.market.entity.payment.Payment;
import com.conel.market.entity.payment.PaymentMethod;
import com.conel.market.entity.payment.PaymentRetryPolicy;
import com.conel.market.entity.payment.PaymentStatus;
import com.conel.market.gateway.payment.PaymentGatewayClient;
import com.conel.market.repository.payment.PaymentRepository;
import com.conel.market.repository.payment.PaymentRetryPolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class PaymentRetryScheduler {

    private final PaymentRepository paymentRepository;
    private final PaymentRetryPolicyRepository paymentRetryPolicyRepository;
    private final Map<PaymentMethod, PaymentGatewayClient> gatewayClients;
    private final PaymentAuditService paymentAuditService;


    public PaymentRetryScheduler(
            PaymentRepository paymentRepository,
            PaymentRetryPolicyRepository paymentRetryPolicyRepository,
            PaymentAuditService paymentAuditService,
            List<PaymentGatewayClient> clients
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentRetryPolicyRepository = paymentRetryPolicyRepository;
        this.paymentAuditService = paymentAuditService;
        this.gatewayClients = clients.stream()
                .collect(java.util.stream.Collectors.toMap(
                        PaymentGatewayClient::supportedMethod, c -> c));
    }

    @Scheduled(fixedDelay = 300000)
    @Transactional
    public void retryFailedPayments() {
        List<Payment> paymentsToRetry = paymentRepository.findPaymentsReadyForRetry(Instant.now());

        if (paymentsToRetry.isEmpty()) {
            return;
        }

        log.info("Retry scheduler found {} payment(s) to retry", paymentsToRetry.size());

        for (Payment payment : paymentsToRetry) {
            try {
                retryPayment(payment);
            } catch (Exception e) {
                log.error("Retry failed for payment {}: {}", payment.getId(), e.getMessage());
            }
        }
    }

    private void retryPayment(Payment payment){
        Optional<PaymentRetryPolicy> policyOpt=paymentRetryPolicyRepository
                .findActiveByPaymentMethod(payment.getPaymentMethod());

        if (policyOpt.isEmpty()) {
            log.warn("No active retry policy for payment method {}, skipping payment {}",
                    payment.getPaymentMethod(), payment.getId());
            return;
        }

        PaymentRetryPolicy policy=policyOpt.get();
        int currentRetryCount=payment.getRetryCount()!=null? payment.getRetryCount() :0;
        long elapsedSeconds=Instant.now().getEpochSecond()-payment.getCreatedAt().getEpochSecond();

        if (!policy.shouldRetry(currentRetryCount, elapsedSeconds)) {
            log.info("Payment {} has exceeded retry limits — marking as final FAILED", payment.getId());
            payment.setNextRetryAt(null);
            paymentRepository.save(payment);
            return;
        }

        PaymentGatewayClient gateway = gatewayClients.get(payment.getPaymentMethod());
        if (gateway == null) {
            log.warn("No gateway found for payment method {}", payment.getPaymentMethod());
            return;
        }

        try {
            PaymentInitiationRequest request=new PaymentInitiationRequest(
                    payment.getOrder().getId(),
                    payment.getPaymentMethod(),
                    payment.getIdempotencyKey() + "-retry-" + (currentRetryCount + 1),
                    payment.getPhoneNumber()
            );

            PaymentGatewayClient.GatewayInitiationResult result=
                    gateway.initiate(request, payment.getAmount(), payment.getIdempotencyKey());

            payment.setMerchantRequestId(result.merchantRequestId());
            payment.setCheckoutRequestId(result.checkoutRequestId());
            payment.setGatewayResponse(result.rawResponse());

            if (result.accepted()) {
                payment.transitionTo(PaymentStatus.PENDING);
                payment.setNextRetryAt(null);
            } else {
                payment.scheduleRetry(policy.getBaseDelaySeconds());
            }

        }catch (Exception e){
            log.error("Gateway call failed during retry for payment {}", payment.getId(), e);
            payment.scheduleRetry(policy.getBaseDelaySeconds());
        }

        paymentRepository.save(payment);
    }
}
