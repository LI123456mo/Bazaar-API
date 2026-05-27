package com.conel.market.models.category.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
        @NotBlank(message = "category name is required")
        String name,
        String description
) {
}
