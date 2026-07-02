package com.conel.market.models.products.dto;

import jakarta.validation.constraints.*;

public record ProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(min = 3, max = 100, message = "Product name must be between 3 and 100 characters")
        String name,

        @NotBlank(message = "Product description is required")
        @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @DecimalMax(value = "999999.99", message = "Price cannot exceed 999,999.99")
        Double price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock cannot be negative")
        @Max(value = 999999, message = "Stock cannot exceed 999,999")
        Integer stockQuantity,

        @NotBlank(message = "Category ID is required")
        String categoryId
) {}