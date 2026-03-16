package com.conel.market.mapper;

import com.conel.market.dto.OrderDto;
import com.conel.market.dto.OrderItemResponseDto;
import com.conel.market.dto.OrderResponseDto;
import com.conel.market.models.Order;
import com.conel.market.models.OrderItem;
import org.springframework.stereotype.Service;

@Service
public class OrderMapper {
    //WHAT THE USER NEEDS TO SEE
    public OrderResponseDto toOrderResponseDto(Order order){
        return new OrderResponseDto(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus().toString(),
                order.getOrderItems()
                        .stream()
                        .map(this::toOrderItemResponseDto)
                        .toList()
        );
    }
    public OrderItemResponseDto toOrderItemResponseDto(OrderItem item){
        return new OrderItemResponseDto(
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPriceAtPurchase()
        );
    }
}
