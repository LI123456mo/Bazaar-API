package com.conel.market.dto.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record OrderRequest(
        @Valid
        List<OrderItemRequest> items,

        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        @NotBlank(message = "Shipping address is required")
        String shippingAddress
) {}