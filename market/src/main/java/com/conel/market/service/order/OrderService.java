package com.conel.market.service.order;

import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.entity.order.Order;
import com.conel.market.entity.order.OrderItem;
import com.conel.market.repository.order.OrderRepository;
import com.conel.market.entity.order.OrderStatus;
import com.conel.market.dto.order.request.OrderRequest;
import com.conel.market.dto.order.request.OrderItemRequest;
import com.conel.market.dto.order.response.OrderItemResponse;
import com.conel.market.dto.order.response.OrderResponse;
import com.conel.market.entity.product.Product;
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

import java.math.BigDecimal; // CHANGE: added
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String authenticatedUserId) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_ORDER);
        }

        User buyer = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .shippingAddress(request.shippingAddress())
                .user(buyer)
                .buyerEmailSnapshot(buyer.getEmail())
                .buyerNameSnapshot(buyer.getFirstName() + " " + buyer.getLastName())
                .orderItems(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .build();


        // BigDecimal is immutable, so  can't do runningTotalAmount += x like a primitive —
        // every operation returns a NEW BigDecimal that we reassign.
        BigDecimal runningTotalAmount = BigDecimal.ZERO;
        List<OrderItemResponse> responseItemsList = new ArrayList<>();

        for (OrderItemRequest itemDto : request.items()) {
            Product lockedProduct = productService.decreaseStock(itemDto.productId(), itemDto.quantity());
            BigDecimal itemPriceAtPurchase = lockedProduct.getPrice();

            BigDecimal itemSubTotal = itemPriceAtPurchase.multiply(BigDecimal.valueOf(itemDto.quantity()));

            runningTotalAmount = runningTotalAmount.add(itemSubTotal);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(lockedProduct)
                    .quantity(itemDto.quantity())
                    .priceAtPurchase(itemPriceAtPurchase)
                    .build();

            order.getOrderItems().add(orderItem);

            responseItemsList.add(new OrderItemResponse(
                    lockedProduct.getId(),
                    lockedProduct.getName(),
                    itemPriceAtPurchase,
                    itemDto.quantity(),
                    itemSubTotal
            ));
        }

        order.setTotalAmount(runningTotalAmount);
        Order savedOrder = orderRepository.save(order);

        log.info("Order created: {} for user: {}", savedOrder.getId(), buyer.getEmail());

        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus().name(),
                savedOrder.getPaymentMethod(),
                savedOrder.getShippingAddress(),
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
                order.getPaymentMethod(),
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
                        order.getPaymentMethod(),
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