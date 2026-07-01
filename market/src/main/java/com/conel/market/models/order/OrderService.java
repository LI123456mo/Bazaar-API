package com.conel.market.models.order;

import com.conel.market.exception.BusinessException;
import com.conel.market.exception.ErrorCode;
import com.conel.market.models.order.dto.request.OrderRequest;
import com.conel.market.models.order.dto.response.OrderItemRequest;
import com.conel.market.models.order.dto.response.OrderItemResponse;
import com.conel.market.models.order.dto.response.OrderResponse;
import com.conel.market.models.products.Product;
import com.conel.market.models.products.ProductService;
import com.conel.market.models.products.dto.ProductResponse;
import com.conel.market.models.user.User;
import com.conel.market.models.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request,String authenticatedUserId) {
        User buyer = userRepository.findById(authenticatedUserId)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));

        Order order = Order.builder()
                .status(OrderStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .shippingAddress(request.shippingAddress())
                .user(buyer)
                .buyerEmailSnapshot(buyer.getEmail())
                .buyerNameSnapshot(buyer.getFirstName()+" "+buyer.getLastName())
                .orderItems(new ArrayList<>())
                .totalAmount(0.0)
                .build();

        double runningTotalAmount = 0.0;
        List<OrderItemResponse> responseItemsList = new ArrayList<>();

        for (OrderItemRequest itemDto : request.items()) {
            Product product = productService.getProductEntity(itemDto.productId());

            if (!product.isActive()) {
                throw new IllegalArgumentException("Cannot purchase archived product: " + product.getName());
            }

            productService.decreaseStock(product.getId(), itemDto.quantity());

            double itemPriceAtPurchase = product.getPrice();
            double itemSubTotal = itemPriceAtPurchase * itemDto.quantity();
            runningTotalAmount += itemSubTotal;

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDto.quantity())
                    .priceAtPurchase(itemPriceAtPurchase) // Lock in historical price
                    .build();

            order.getOrderItems().add(orderItem);

            responseItemsList.add(new OrderItemResponse(
                    product.getId(),
                    product.getName(),
                    itemPriceAtPurchase,
                    itemDto.quantity(),
                    itemSubTotal
            ));
        }

        order.setTotalAmount(runningTotalAmount);


        Order savedOrder = orderRepository.save(order);


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
    public OrderResponse getOrderById(String orderId,String authenticatedUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + orderId));

        validateOwnership(order,authenticatedUserId);

        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getPriceAtPurchase(),
                        item.getQuantity(),
                        item.getPriceAtPurchase() * item.getQuantity()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus().name(),
                order.getPaymentMethod(),
                order.getShippingAddress(),
                itemResponses
        );
    }

    // OrderService.java
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(order -> {
                    List<OrderItemResponse> itemResponses=order.getOrderItems().stream()
                            .map(item->new OrderItemResponse(
                                    item.getProduct().getId(),
                                    item.getProduct().getName(),
                                    item.getPriceAtPurchase(),
                                    item.getQuantity(),
                                    item.getPriceAtPurchase()* item.getQuantity()
                            ))
                            .toList();

                    return new OrderResponse(
                            order.getId(),
                            order.getTotalAmount(),
                            order.getStatus().name(),
                            order.getPaymentMethod(),
                            order.getShippingAddress(),
                            itemResponses
                    );
                });
    }


    public void validateOwnership(Order order,String authenticatedUserId){
        if (!order.getUser().getId().equals(authenticatedUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}