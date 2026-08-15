package com.conel.market.dto.payment;

import com.conel.market.entity.payment.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Example:
 * {
 *   "orderId": "550e8400-e29b-41d4-a716-446655440000",
 *   "paymentMethod": "M_PESA",
 *   "phoneNumber": "254712345678",
 *   "idempotencyKey": "650e8400-e29b-41d4-a716-446655440001"
 * }
 */
public record PaymentInitiationRequest(
    @NotBlank(message = "Order ID is required")
    String orderId,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,

    @NotBlank(message = "Idempotency key is required")
    String idempotencyKey,

    @Pattern(regexp = "^254\\d{9}$|^0\\d{9}$", message = "Phone must be international (254...) or national (0...)")
    String phoneNumber
) {}
