package com.conel.market.dto.order.response;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productId,
        String productName,
        BigDecimal priceAtPurchase,
        Integer quantity,
        BigDecimal subTotal
) {}