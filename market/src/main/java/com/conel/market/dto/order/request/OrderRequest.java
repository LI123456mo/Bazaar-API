package com.conel.market.dto.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record OrderRequest(
        @NotEmpty(message = "An order must contain at least one item")
        @Valid
        List<OrderItemRequest> items,

        @NotBlank(message = "User ID is required to place an order")
        String userId,

        @NotBlank(message = "Payment method is required")
        String paymentMethod,

        @NotBlank(message = "Shipping address is required")
        String shippingAddress
) {}