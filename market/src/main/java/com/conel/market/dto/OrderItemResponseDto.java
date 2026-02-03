package com.conel.market.dto;

public record OrderItemResponseDto(
        String productName,
        Integer quantity,
        Double priceAtPurchase
        ) {
}
