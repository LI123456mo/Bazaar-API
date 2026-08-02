package com.conel.market.models.cart;

import com.conel.market.models.cart.dto.AddToCartRequest;
import com.conel.market.models.cart.dto.CartResponse;
import com.conel.market.models.cart.dto.UpdateCartItemRequest;
import com.conel.market.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The cart controller exposes the customer-facing operations for persistent cart management.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Cart management for customers")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("hasAuthority('cart:read')")
    @Operation(summary = "Get my cart", description = "Returns the current user's cart with items, totals, and vendor count")
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(cartService.getCart(currentUser.getId()));
    }

    @PostMapping("/items")
    @PreAuthorize("hasAuthority('cart:write')")
    @Operation(summary = "Add item to cart", description = "Adds a product to the authenticated user's cart")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(currentUser.getId(), request));
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("hasAuthority('cart:write')")
    @Operation(summary = "Update cart item quantity", description = "Changes the quantity or removes the item when the quantity is zero")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(currentUser.getId(), cartItemId, request));
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("hasAuthority('cart:write')")
    @Operation(summary = "Remove cart item", description = "Deletes a single line item from the cart")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String cartItemId) {
        cartService.removeFromCart(currentUser.getId(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasAuthority('cart:write')")
    @Operation(summary = "Clear cart", description = "Removes all items from the authenticated user's cart")
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal User currentUser) {
        cartService.clearCart(currentUser.getId());
        return ResponseEntity.noContent().build();
    }
}
