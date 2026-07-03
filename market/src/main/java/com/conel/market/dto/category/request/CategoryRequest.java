package com.conel.market.dto.category.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "category name is required")
        @Size(max = 50, message = "Category name must not exceed 50 characters")
        String name,
        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {
}
