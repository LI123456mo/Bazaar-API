package com.conel.market.models.category.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CategoryUpdateRequest(
        @NotBlank(message = "VALIDATION.CATEGORY.NAME.NOT_BLANK")
        String name,

        @NotBlank(message = "VALIDATION.CATEGORY.DESCRIPTION.NOT_BLANK")
        String description
) {
}