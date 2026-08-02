package com.conel.market.cart.dto;

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