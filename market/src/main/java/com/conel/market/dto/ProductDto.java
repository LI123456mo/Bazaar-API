package com.conel.market.dto;

import jakarta.validation.constraints.*;

public record ProductDto(
        @NotBlank(message = "Product name is required")
        @Size(min = 2,max = 100,message = "Name must be between 2 and 100 characters")
        String name,
        @NotBlank(message = "Description can not be empty")
        String description,
        @NotNull(message = "price is required")
        @Positive(message = "price must be greater than zero")
        Double price,
        @Min(value = 0,message = "stock cannot be negative")
        Integer stockQuantity,
        @NotNull(message = "Category ID is required")
        Integer categoryId
) {
}
