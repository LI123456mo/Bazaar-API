package com.conel.market.controller;

import com.conel.market.dto.payment.DarajaWebhookPayload;
import com.conel.market.dto.payment.PaymentInitiationRequest;
import com.conel.market.dto.payment.PaymentInitiationResponse;
import com.conel.market.dto.payment.PaymentStatusResponse;
import com.conel.market.service.payment.PaymentService;
import com.conel.market.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;

    @PostMapping("/initiate")
    @PreAuthorize("hasAuthority('payment:create')")
    public ResponseEntity<PaymentInitiationResponse> initiatePayment(
            @Valid @RequestBody PaymentInitiationRequest request,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        PaymentInitiationResponse response =
                paymentService.initiationResponse(request, authenticatedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAuthority('payment:read')")
    public ResponseEntity<PaymentStatusResponse> getPaymentStatus(
            @PathVariable String paymentId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        PaymentStatusResponse response =
                paymentService.getPaymentStatus(paymentId, authenticatedUser.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook/mpesa")
    public ResponseEntity<String> handleMpesaWebhook(@RequestBody String rawBody) {
        log.info("Received M-Pesa webhook");
        try {
            DarajaWebhookPayload payload = objectMapper.readValue(rawBody, DarajaWebhookPayload.class);
            paymentService.handleMpesaWebhook(payload, rawBody);
        } catch (Exception e) {
            log.error("Failed to process M-Pesa webhook", e);
        }
        return ResponseEntity.ok("{\"ResultCode\":0,\"ResultDesc\":\"Accepted\"}");
    }
}