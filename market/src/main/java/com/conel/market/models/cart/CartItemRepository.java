package com.conel.market.models.cart;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CartItemRepository extends JpaRepository<CartItem, String> {

    Optional<CartItem> findByCartIdAndProductId(String cartId, String productId);

    void deleteByCartId(String cartId);
}
