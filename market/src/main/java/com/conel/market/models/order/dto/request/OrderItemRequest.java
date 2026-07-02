package com.conel.market.models.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotBlank(message = "Product ID is required")
        String productId,

        @Positive(message = "Quantity must be greater than 0")
        Integer quantity
) {}