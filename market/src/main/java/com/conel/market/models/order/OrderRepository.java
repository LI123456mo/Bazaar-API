package com.conel.market.models.order;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,String> {

    @EntityGraph(attributePaths = {"user","orderItems","orderItems.product"})
    Optional<Order> findById(String id);
}
