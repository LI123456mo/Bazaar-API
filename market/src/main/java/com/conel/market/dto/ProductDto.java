package com.conel.market.dto;

public record ProductDto(
        String name,
        String description,
        Double price,
        Integer stockQuantity,
        Integer categoryId
) {
}
