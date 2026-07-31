package com.conel.market.dto.order.response;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        String id,
        BigDecimal totalAmount,
        String status,
        String paymentMethod,
        String shippingAddress,
        List<OrderItemResponse> items
) {}