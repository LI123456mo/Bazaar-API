package com.conel.market.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryDto(
        @NotBlank(message = "category name is required")
        String name,
        String description
) {
}
