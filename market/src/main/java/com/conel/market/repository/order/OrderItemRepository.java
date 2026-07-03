package com.conel.market.repository.order;

import com.conel.market.entity.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem,String> {
}
