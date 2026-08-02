package com.conel.market.cart.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * The response carries the vendor count so the checkout experience can warn the customer about split orders.
 */
public record CartResponse(
        String cartId,
        List<CartItemResponse> items,
        int totalItems,
        BigDecimal totalPrice,
        int vendorCount
) {
}
