package com.conel.market.dto.product.response;

public record ProductResponse(
        String id,
        String name,
        Double price,
        String imageUrl,
        String categoryName,
        String sellerDisplayName
) {}