package com.conel.market.dto.product.response;

import java.math.BigDecimal;

public record ProductResponse(
        String id,
        String name,
        BigDecimal price,
        String imageUrl,
        String categoryName,
        String sellerDisplayName
) {}