package com.conel.market.models.cart;

import com.conel.market.entity.product.Product;
import com.conel.market.models.cart.dto.CartItemResponse;
import com.conel.market.models.cart.dto.CartResponse;
import com.conel.market.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * The mapper centralizes cart response shaping so the controller stays thin and the frontend sees consistent payloads.
 */
@Component
public class CartMapper {

    public CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems() == null ? List.of() : cart.getItems().stream()
                .map(this::toCartItemResponse)
                .toList();

        long vendorCount = items.stream()
                .map(CartItemResponse::vendorId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        return new CartResponse(
                cart.getId(),
                items,
                cart.getTotalItems(),
                cart.getTotalPrice(),
                Math.toIntExact(vendorCount)
        );
    }

    public CartItemResponse toCartItemResponse(CartItem cartItem) {
        Product product = cartItem.getProduct();
        boolean inStock = product != null
                && product.getStockQuantity() != null
                && product.getStockQuantity() > 0;

        User seller = product != null ? product.getSeller() : null;
        String vendorName = seller == null ? null : buildVendorName(seller);

        return new CartItemResponse(
                cartItem.getId(),
                product != null ? product.getId() : null,
                product != null ? product.getName() : null,
                product != null ? product.getImageUrl() : null,
                product != null ? product.getPrice() : null,
                cartItem.getQuantity(),
                cartItem.getSubtotal(),
                seller != null ? seller.getId() : null,
                vendorName,
                inStock
        );
    }

    private String buildVendorName(User seller) {
        String firstName = seller.getFirstName() == null ? "" : seller.getFirstName();
        String lastName = seller.getLastName() == null ? "" : seller.getLastName();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? seller.getEmail() : fullName;
    }
}
