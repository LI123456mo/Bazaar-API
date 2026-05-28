package com.conel.market.models.order.dto;

import java.util.List;

public record OrderResponse(
        String id, // Fixed: Swapped to String to support your UUID database keys
        Double totalAmount,
        String status,
        String paymentMethod,
        String shippingAddress,
        List<OrderItemResponse> items
) {}