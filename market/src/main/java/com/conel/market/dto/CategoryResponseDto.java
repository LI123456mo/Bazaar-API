package com.conel.market.dto;
import java.time.LocalDateTime;

public record CategoryResponseDto(Integer id, String name, LocalDateTime createdAt,String createdBy,String description) {
}
