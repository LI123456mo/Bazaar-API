package com.conel.market.dto.order.response;

public record OrderItemResponse(
        String productId,
        String productName,
        Double priceAtPurchase,
        Integer quantity,
        Double subTotal
) {}