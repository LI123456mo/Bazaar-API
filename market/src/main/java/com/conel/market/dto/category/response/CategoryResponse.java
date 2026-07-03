package com.conel.market.dto.category.response;

import java.time.LocalDateTime;

public record CategoryResponse(String id, String name, String description, LocalDateTime lastModifiedAt,String createdBy) {
}
