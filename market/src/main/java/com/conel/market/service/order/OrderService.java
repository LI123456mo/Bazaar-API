package com.conel.market.service.order;

import com.conel.market.cart.Cart;
import com.conel.market.cart.CartItem;
import com.conel.market.cart.CartOwner;
import com.conel.market.cart.CartRepository;
import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.entity.order.Order;
import com.conel.market.entity.order.OrderItem;
import com.conel.market.entity.payment.PaymentStatus;
import com.conel.market.repository.order.OrderRepository;
import com.conel.market.entity.order.OrderStatus;
import com.conel.market.dto.order.request.OrderRequest;
import com.conel.market.dto.order.response.OrderItemResponse;
import com.conel.market.dto.order.response.OrderResponse;
import com.conel.market.entity.product.Product;
import com.conel.market.cart.CartService;
import com.conel.market.service.product.ProductService;
import com.conel.market.user.entity.User;
import com.conel.market.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final CartService cartService;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String authenticatedUserId) {
        User buyer = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Cart cart = cartRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EMPTY_ORDER));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_ORDER);
        }

        Map<String, Order> ordersByVendor = new LinkedHashMap<>();
        BigDecimal runningTotalAmount = BigDecimal.ZERO;
        List<OrderItemResponse> responseItemsList = new ArrayList<>();

        for (CartItem cartItem : cart.getItems()) {
            Product lockedProduct = productService.decreaseStock(cartItem.getProduct().getId(), cartItem.getQuantity());
            String sellerId = lockedProduct.getSeller() != null ? lockedProduct.getSeller().getId() : "unknown";
            Order vendorOrder = ordersByVendor.computeIfAbsent(sellerId, ignored -> buildOrder(buyer, request));

            BigDecimal itemPriceAtPurchase = lockedProduct.getPrice();
            BigDecimal itemSubTotal = itemPriceAtPurchase.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            runningTotalAmount = runningTotalAmount.add(itemSubTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(vendorOrder)
                    .product(lockedProduct)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(itemPriceAtPurchase)
                    .build();

            vendorOrder.getOrderItems().add(orderItem);
            vendorOrder.setTotalAmount(vendorOrder.getTotalAmount().add(itemSubTotal));

            responseItemsList.add(new OrderItemResponse(
                    lockedProduct.getId(),
                    lockedProduct.getName(),
                    itemPriceAtPurchase,
                    cartItem.getQuantity(),
                    itemSubTotal
            ));
        }

        List<Order> savedOrders = new ArrayList<>();
        for (Order vendorOrder : ordersByVendor.values()) {
            savedOrders.add(orderRepository.save(vendorOrder));
        }

        cartService.clearCart(CartOwner.ofUser(authenticatedUserId));

        log.info("Created {} vendor orders for user {}", savedOrders.size(), buyer.getEmail());

        return new OrderResponse(
                savedOrders.isEmpty() ? null : savedOrders.get(0).getId(),
                runningTotalAmount,
                OrderStatus.PENDING.name(),
                request.paymentMethod().name(),
                request.shippingAddress(),
                responseItemsList
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId, String authenticatedUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        validateOwnership(order, authenticatedUserId);

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getPaymentMethod().name(),
                order.getShippingAddress(),
                toOrderItemResponses(order)
        );
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(order -> new OrderResponse(
                        order.getId(),
                        order.getTotalAmount(),
                        order.getStatus().name(),
                        order.getPaymentMethod().name(),
                        order.getShippingAddress(),
                        toOrderItemResponses(order)
                ));
    }

    private List<OrderItemResponse> toOrderItemResponses(Order order) {
        return order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getPriceAtPurchase(),
                        item.getQuantity(),
                        item.getPriceAtPurchase().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();
    }

    private Order buildOrder(User buyer, OrderRequest request) {
        return Order.builder()
                .status(OrderStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .paymentStatus(PaymentStatus.INITIATED)
                .shippingAddress(request.shippingAddress())
                .user(buyer)
                .buyerEmailSnapshot(buyer.getEmail())
                .buyerNameSnapshot(buyer.getFirstName() + " " + buyer.getLastName())
                .orderItems(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .build();
    }

    public void validateOwnership(Order order, String authenticatedUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "order:read_all".equals(a.getAuthority()))) {
            return;
        }
        if (!order.getUser().getId().equals(authenticatedUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}