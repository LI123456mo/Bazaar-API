package com.conel.market.dto.payment;

import com.conel.market.entity.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response after payment initiation.
 * Returned to client immediately after STK push sent.
 * 
 * Contains:
 * - paymentId: Reference for tracking
 * - status: Current state (INITIATED, PENDING, etc.)
 * - amount: Total amount for transaction
 * - message: User-friendly message
 * - nextAction: What user should do next (e.g., "Check phone for M-Pesa prompt")
 * 
 * Example response:
 * {
 *   "paymentId": "750e8400-e29b-41d4-a716-446655440002",
 *   "status": "PENDING",
 *   "amount": "5000.00",
 *   "currency": "KES",
 *   "message": "Payment initiated successfully",
 *   "nextAction": "Check your phone for M-Pesa STK push prompt",
 *   "initiatedAt": "2026-08-05T17:25:30Z"
 * }
 */
public record PaymentInitiationResponse(
    String paymentId,
    PaymentStatus status,
    BigDecimal amount,
    String currency,
    String message,
    String nextAction,
    LocalDateTime initiatedAt
) {}
