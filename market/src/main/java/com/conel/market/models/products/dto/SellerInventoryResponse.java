package com.conel.market.models.products.dto;

public record SellerInventoryResponse(
        String id,
        String name,
        Integer stockQuantity,
        Double price,
        boolean active // They need to see if their item is active or archived by admins
) {}