package com.conel.market.service.payment;

import com.conel.market.dto.payment.DarajaWebhookPayload;
import com.conel.market.dto.payment.PaymentInitiationRequest;
import com.conel.market.dto.payment.PaymentInitiationResponse;
import com.conel.market.dto.payment.PaymentStatusResponse;
import com.conel.market.entity.order.Order;
import com.conel.market.entity.payment.*;
import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.gateway.payment.PaymentGatewayClient;
import com.conel.market.repository.order.OrderRepository;
import com.conel.market.repository.payment.PaymentRepository;
import com.conel.market.repository.payment.PaymentTransactionRepository;
import com.conel.market.user.entity.User;
import com.conel.market.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final Map<PaymentMethod, PaymentGatewayClient> gatewayClients;
    private static final String DEFAULT_CURRENCY = "KES";

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            List<PaymentGatewayClient> clients
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.gatewayClients = clients.stream()
                .collect(Collectors.toMap(PaymentGatewayClient::supportedMethod, c -> c));
    }


    @Transactional
    public PaymentInitiationResponse initiationResponse(PaymentInitiationRequest request, String authenticatedUserId){

        var existing = paymentRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            log.info("Duplicate payment initiation request for idempotency key {}", request.idempotencyKey());
            return toInitiationResponse(existing.get());
        }

        User buyer = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));


        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(authenticatedUserId)){
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (order.getPaymentStatus()== PaymentStatus.COMPLETED){
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PAID);
        }

        Payment payment=Payment.builder()
                .order(order)
                .user(buyer)
                .idempotencyKey(request.idempotencyKey())
                .amount(order.getTotalAmount())
                .currency(DEFAULT_CURRENCY)
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.INITIATED)
                .phoneNumber(request.phoneNumber())
                .initiatedAt(Instant.now())
                .retryCount(0)
                .reconciled(false)
                .build();


        PaymentGatewayClient gateway=gatewayClients.get(request.paymentMethod());
        if (gateway == null) {
            throw new BusinessException(ErrorCode.PAYMENT_INITIATION_FAILED,
                    "No gateway configured for payment method " + request.paymentMethod());
        }

        PaymentGatewayClient.GatewayInitiationResult result=
                gateway.initiate(request,order.getTotalAmount(), request.idempotencyKey());

        //tracking reference for payment
        payment.setMerchantRequestId(result.merchantRequestId());
        payment.setCheckoutRequestId(result.checkoutRequestId());
        payment.setGatewayResponse(result.rawResponse());

        if (result.accepted()){
            payment.transitionTo(PaymentStatus.PENDING);
        } else {
            payment.transitionTo(PaymentStatus.FAILED);
            payment.setErrorMessage("Payment initiation rejected by gateway");
        }

        paymentRepository.save(payment);
        order.setPaymentStatus(payment.getStatus());
        orderRepository.save(order);
        return toInitiationResponse(payment);
    }

    @Transactional
    public void handleMpesaWebhook(DarajaWebhookPayload payload, String rawBody){
        DarajaWebhookPayload.StkCallback callback=payload.body().stkCallback();
        String checkoutRequestId=callback.checkoutRequestId();

        Optional<PaymentTransaction> existingTxn=
                paymentTransactionRepository.findByExternalTransactionId(checkoutRequestId);

        if (existingTxn.isPresent()) {
            log.info("Duplicate webhook received for checkoutRequestId {}", checkoutRequestId);
            return;
        }

        PaymentTransaction txn= PaymentTransaction.builder()
                .externalTransactionId(checkoutRequestId)
                .transactionType(TransactionType.WEBHOOK)
                .payload(rawBody)
                .status(TransactionStatus.RECEIVED)
                .signatureVerified(true)
                .build();

        Payment payment=paymentRepository.findByCheckoutRequestId(checkoutRequestId).orElse(null);

        if (payment == null) {
            txn.setStatus(TransactionStatus.DISCARDED);
            txn.setErrorMessage("No matching Payment found for checkoutRequestId " + checkoutRequestId);
            paymentTransactionRepository.save(txn);
            log.warn("Webhook rejected — no matching payment for checkoutRequestId {}", checkoutRequestId);
            return;
        }

        txn.setPayment(payment);

        boolean success=callback.resultCode()!=null && callback.resultCode()==0;

        if ((success)){
            String mpesaReceiptNumber= extractMetadataValue(callback, "MpesaReceiptNumber");
            payment.setExternalTransactionRef(mpesaReceiptNumber);
            payment.transitionTo(PaymentStatus.COMPLETED);
        }else {
            payment.setErrorMessage(callback.resultDesc());
            payment.transitionTo(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);

        Order order=payment.getOrder();
        order.setPaymentStatus(payment.getStatus());
        orderRepository.save(order);

        txn.setStatus(TransactionStatus.PROCESSED);
        txn.setProcessed(true);
        txn.setProcessedAt(Instant.now());
        paymentTransactionRepository.save(txn);

        log.info("Payment {} for order {} marked {}", payment.getId(), order.getId(), payment.getStatus());
    }

    @Transactional(readOnly = true)
    public PaymentStatusResponse getPaymentStatus(String paymentId, String authenticatedUserId){

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getUser().getId().equals(authenticatedUserId)){
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return PaymentStatusResponse.from(payment);
    }

    private String extractMetadataValue(DarajaWebhookPayload.StkCallback callback, String name) {
        if (callback.callbackMetadata() == null) return null;
        return callback.callbackMetadata().items().stream()
                .filter(item -> name.equals(item.name()))
                .map(item -> String.valueOf(item.value()))
                .findFirst()
                .orElse(null);
    }

    private PaymentInitiationResponse toInitiationResponse(Payment payment) {
        String nextAction = switch (payment.getStatus()) {
            case PENDING -> "Check your phone for the M-Pesa STK push prompt";
            case FAILED -> "Payment could not be started — please try again";
            default -> "Awaiting further processing";
        };

        return new PaymentInitiationResponse(
                payment.getId(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                "Payment initiated",
                nextAction,
                payment.getInitiatedAt()
        );
    }
}
