package com.conel.market.dto;

import java.util.List;

public record OrderResponseDto(
        Integer id,
        Double totalAmount,
        String status,
        List<OrderItemResponseDto> items
) {
}
