package com.conel.market.dto;

import java.util.List;

public record OrderDto(Integer userId, List<OrderItemDto> items) {
}
