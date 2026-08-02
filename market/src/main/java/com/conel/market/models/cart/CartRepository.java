package com.conel.market.models.cart;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.seller", "items.product.category"})
    Optional<Cart> findByUserId(String userId);

    @EntityGraph(attributePaths = {"items", "items.product", "items.product.seller", "items.product.category"})
    Optional<Cart> findByGuestToken(String guestToken);

    boolean existsByUserId(String userId);
}