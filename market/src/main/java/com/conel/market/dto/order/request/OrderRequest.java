package com.conel.market.dto.order.request;

import com.conel.market.entity.payment.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(
        @Valid
        List<OrderItemRequest> items,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @NotBlank(message = "Shipping address is required")
        String shippingAddress
) {}