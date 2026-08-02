package com.conel.market.models.cart;

import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.entity.product.Product;
import com.conel.market.models.cart.dto.AddToCartRequest;
import com.conel.market.models.cart.dto.CartResponse;
import com.conel.market.models.cart.dto.UpdateCartItemRequest;
import com.conel.market.repository.product.ProductRepository;
import com.conel.market.user.entity.User;
import com.conel.market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartMapper cartMapper;

    @Transactional(readOnly = true)
    public CartResponse getCart(String userId) {
        return cartMapper.toCartResponse(getOrCreateCart(userId));
    }

    public CartResponse addToCart(String userId, AddToCartRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        if (!product.isActive()) {
            throw new BusinessException(ErrorCode.PRODUCT_ARCHIVED);
        }

        if (product.getStockQuantity() == null || product.getStockQuantity() < request.quantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.quantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
            }
            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.quantity())
                    .build();
            cart.getItems().add(cartItem);
            cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart);
        log.info("Added {} units of product {} to cart {}", request.quantity(), product.getId(), userId);
        return cartMapper.toCartResponse(getOrCreateCart(userId));
    }

    public CartResponse updateCartItem(String userId, String cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (cartItem.getCart() == null || !cart.getId().equals(cartItem.getCart().getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (request.quantity() == 0) {
            cart.getItems().remove(cartItem);
            cartRepository.save(cart);
            log.info("Removed cart item {} from cart {}", cartItemId, userId);
            return cartMapper.toCartResponse(getOrCreateCart(userId));
        }

        Product product = cartItem.getProduct();
        if (product.getStockQuantity() == null || product.getStockQuantity() < request.quantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        cartItem.setQuantity(request.quantity());
        cartItemRepository.save(cartItem);
        log.info("Updated cart item {} quantity to {} for cart {}", cartItemId, request.quantity(), userId);
        return cartMapper.toCartResponse(getOrCreateCart(userId));
    }

    public CartResponse removeFromCart(String userId, String cartItemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (cartItem.getCart() == null || !cart.getId().equals(cartItem.getCart().getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        cart.getItems().remove(cartItem);
        cartRepository.save(cart);
        log.info("Removed cart item {} from cart {}", cartItemId, userId);
        return cartMapper.toCartResponse(getOrCreateCart(userId));
    }

    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Cleared cart {}", userId);
    }

    private Cart getOrCreateCart(CartOwner owner) {
        if (owner.isGuest()){
            return cartRepository.findByGuestToken(owner.guestToken())
                    .orElseGet(()->cartRepository.save(
                            Cart.builder().guestToken(owner.guestToken()).build()
                    ));
        }

        return cartRepository.findByUserId(owner.userId())
                .orElseGet(() -> {
                    User user = userRepository.findById(owner.userId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
                    return cartRepository.save(Cart.builder().user(user).build());
                });
    }
}
