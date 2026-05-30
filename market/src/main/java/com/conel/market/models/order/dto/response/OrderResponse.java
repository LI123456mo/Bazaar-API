package com.conel.market.models.order.dto.response;

import java.util.List;

public record OrderResponse(
        String id,
        Double totalAmount,
        String status,
        String paymentMethod,
        String shippingAddress,
        List<OrderItemResponse> items
) {}