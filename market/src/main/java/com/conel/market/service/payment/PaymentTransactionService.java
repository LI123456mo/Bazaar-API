package com.conel.market.service.payment;

import com.conel.market.entity.payment.PaymentTransaction;
import com.conel.market.entity.payment.TransactionStatus;
import com.conel.market.entity.payment.TransactionType;
import com.conel.market.repository.payment.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;

    public void recordRejectedWebhook(String rawBody, String sourceIp) {
        PaymentTransaction txn = PaymentTransaction.builder()
                .transactionType(TransactionType.WEBHOOK)
                .payload(rawBody)
                .sourceIpAddress(sourceIp)
                .status(TransactionStatus.DISCARDED)
                .signatureVerified(false)
                .errorMessage("Rejected — source IP not in Safaricom allowlist")
                .build();

        paymentTransactionRepository.save(txn);
    }
}