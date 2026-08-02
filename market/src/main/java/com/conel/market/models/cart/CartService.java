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
    public CartResponse getCart(CartOwner owner) {
        return cartMapper.toCartResponse(getOrCreateCart(owner));
    }

    public CartResponse addToCart(CartOwner owner, AddToCartRequest request) {
        Cart cart = getOrCreateCart(owner);
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
        log.info("Added {} units of product {} to cart {}", request.quantity(), product.getId(), cart.getId());
        return cartMapper.toCartResponse(getOrCreateCart(owner));
    }

    public CartResponse updateCartItem(CartOwner owner, String cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(owner);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (cartItem.getCart() == null || !cart.getId().equals(cartItem.getCart().getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        if (request.quantity() == 0) {
            cart.getItems().remove(cartItem);
            cartRepository.save(cart);
            log.info("Removed cart item {} from cart {}", cartItemId, cart.getId());
            return cartMapper.toCartResponse(getOrCreateCart(owner));
        }

        Product product = cartItem.getProduct();
        if (product.getStockQuantity() == null || product.getStockQuantity() < request.quantity()) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }

        cartItem.setQuantity(request.quantity());
        cartItemRepository.save(cartItem);
        log.info("Updated cart item {} quantity to {} for cart {}", cartItemId, request.quantity(), cart.getId());
        return cartMapper.toCartResponse(getOrCreateCart(owner));
    }

    public CartResponse removeFromCart(CartOwner owner, String cartItemId) {
        Cart cart = getOrCreateCart(owner);
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_ITEM_NOT_FOUND));

        if (cartItem.getCart() == null || !cart.getId().equals(cartItem.getCart().getId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        cart.getItems().remove(cartItem);
        cartRepository.save(cart);
        log.info("Removed cart item {} from cart {}", cartItemId, cart.getId());
        return cartMapper.toCartResponse(getOrCreateCart(owner));
    }

    public void clearCart(CartOwner owner) {
        Cart cart = getOrCreateCart(owner);
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Cleared cart {}", cart.getId());
    }

    public void mergeGuestCartIntoUser(String guestToken, String userId) {
        Optional<Cart> guestCartOpt = cartRepository.findByGuestToken(guestToken);
        if (guestCartOpt.isEmpty() || guestCartOpt.get().getItems().isEmpty()) {
            return;
        }
        Cart guestCart = guestCartOpt.get();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Cart userCart = cartRepository.findByUserId(userId)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        for (CartItem guestItem : guestCart.getItems()) {
            Optional<CartItem> matchingUserItem = cartItemRepository
                    .findByCartIdAndProductId(userCart.getId(), guestItem.getProduct().getId());

            if (matchingUserItem.isPresent()) {
                CartItem userItem = matchingUserItem.get();
                int mergedQuantity = userItem.getQuantity() + guestItem.getQuantity();
                int cappedQuantity = Math.min(mergedQuantity, safeStock(guestItem.getProduct()));
                userItem.setQuantity(Math.max(cappedQuantity, 1));
                cartItemRepository.save(userItem);
            } else {
                CartItem movedItem = CartItem.builder()
                        .cart(userCart)
                        .product(guestItem.getProduct())
                        .quantity(Math.min(guestItem.getQuantity(), safeStock(guestItem.getProduct())))
                        .build();
                userCart.getItems().add(movedItem);
                cartItemRepository.save(movedItem);
            }
        }

        cartRepository.save(userCart);
        cartItemRepository.deleteByCartId(guestCart.getId());
        cartRepository.delete(guestCart);

        log.info("Merged guest cart {} into user {} cart {}", guestCart.getId(), userId, userCart.getId());
    }

    private int safeStock(Product product) {
        return product.getStockQuantity() == null ? 0 : product.getStockQuantity();
    }

    private Cart getOrCreateCart(CartOwner owner) {
        if (owner.isGuest()) {
            return cartRepository.findByGuestToken(owner.guestToken())
                    .orElseGet(() -> cartRepository.save(
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