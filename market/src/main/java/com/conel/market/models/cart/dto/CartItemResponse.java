package com.conel.market.models.cart.dto;

import java.math.BigDecimal;


public record CartItemResponse(
        String cartItemId,
        String productId,
        String productName,
        String productImageUrl,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {
}