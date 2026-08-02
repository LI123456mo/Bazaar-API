package com.conel.market.models.cart;

import com.conel.market.models.cart.dto.AddToCartRequest;
import com.conel.market.models.cart.dto.CartResponse;
import com.conel.market.models.cart.dto.UpdateCartItemRequest;
import com.conel.market.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Cart management for guests and customers")
public class CartController {

    private final CartService cartService;
    private final GuestTokenService guestTokenService;

    @GetMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Get my cart", description = "Returns the current cart (guest or authenticated) with items and totals")
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(cartService.getCart(resolveOwner(currentUser, request, response)));
    }

    @PostMapping("/items")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Add item to cart", description = "Adds a product to the current guest or user cart")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody AddToCartRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        CartOwner owner = resolveOwner(currentUser, httpRequest, httpResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addToCart(owner, request));
    }

    @PutMapping("/items/{cartItemId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Update cart item quantity", description = "Changes the quantity or removes the item when the quantity is zero")
    public ResponseEntity<CartResponse> updateCartItem(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        CartOwner owner = resolveOwner(currentUser, httpRequest, httpResponse);
        return ResponseEntity.ok(cartService.updateCartItem(owner, cartItemId, request));
    }

    @DeleteMapping("/items/{cartItemId}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Remove cart item", description = "Deletes a single line item from the cart")
    public ResponseEntity<Void> removeFromCart(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String cartItemId,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        CartOwner owner = resolveOwner(currentUser, httpRequest, httpResponse);
        cartService.removeFromCart(owner, cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("permitAll()")
    @Operation(summary = "Clear cart", description = "Removes all items from the current cart")
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {
        CartOwner owner = resolveOwner(currentUser, httpRequest, httpResponse);
        cartService.clearCart(owner);
        return ResponseEntity.noContent().build();
    }

    private CartOwner resolveOwner(User currentUser, HttpServletRequest request, HttpServletResponse response) {
        if (currentUser != null) {
            return CartOwner.ofUser(currentUser.getId());
        }
        String existingToken = guestTokenService.readToken(request);
        if (existingToken != null) {
            return CartOwner.ofGuest(existingToken);
        }
        return CartOwner.ofGuest(guestTokenService.issueNewToken(response));
    }
}