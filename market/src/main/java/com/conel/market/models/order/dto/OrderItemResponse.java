package com.conel.market.models.order.dto;

public record OrderItemResponse(
        String productId,
        String productName,
        Double priceAtPurchase,
        Integer quantity,
        Double subTotal
) {}