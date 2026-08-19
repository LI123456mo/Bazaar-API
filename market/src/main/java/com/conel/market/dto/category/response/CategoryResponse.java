package com.conel.market.dto.category.response;

import java.time.Instant;
import java.time.LocalDateTime;

public record CategoryResponse(String id, String name, String description, Instant lastModifiedAt, String createdBy) {
}
