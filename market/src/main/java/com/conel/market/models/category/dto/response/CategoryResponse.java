package com.conel.market.models.category.dto.response;

import java.time.LocalDateTime;

public record CategoryResponse(String id, String name, String description, LocalDateTime lastModifiedAt,String createdBy) {
}
