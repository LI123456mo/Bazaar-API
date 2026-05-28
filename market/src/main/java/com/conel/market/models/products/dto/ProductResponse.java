package com.conel.market.models.products.dto;

public record ProductResponse(
        String id,
        String name,
        Double price,
        String imageUrl,
        String categoryName,
        String sellerDisplayName
) {}