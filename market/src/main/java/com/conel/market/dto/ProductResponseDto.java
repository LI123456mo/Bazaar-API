package com.conel.market.dto;

import java.time.LocalDateTime;

public record ProductResponseDto(
        Integer id,
        String name,
        String description,
        Double price,
        Integer stockQuantity,
        String categoryName,
        LocalDateTime createdAt
) {
}
