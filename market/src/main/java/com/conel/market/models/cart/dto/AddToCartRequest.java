package com.conel.market.models.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * The request validates the incoming cart quantity so customers cannot add unrealistic line item sizes.
 */
public record AddToCartRequest(
        @NotNull String productId,
        @NotNull @Min(1) @Max(100) Integer quantity
) {
}
