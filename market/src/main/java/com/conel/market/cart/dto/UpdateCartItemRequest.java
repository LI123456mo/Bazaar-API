package com.conel.market.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * A quantity of zero means remove the item; the service handles that transition explicitly.
 */
public record UpdateCartItemRequest(
        @NotNull @Min(0) @Max(100) Integer quantity
) {
}
