package com.conel.market.models.order.dto;

import com.conel.market.models.order.OrderItem;
import com.conel.market.models.order.OrderStatus;
import com.conel.market.models.order.Order;
import com.conel.market.models.order.OrderRepository;
import com.conel.market.models.order.dto.request.OrderRequest;
import com.conel.market.models.order.dto.response.OrderItemRequest;
import com.conel.market.models.order.dto.response.OrderItemResponse;
import com.conel.market.models.order.dto.response.OrderResponse;
import com.conel.market.models.products.Product;
import com.conel.market.models.products.ProductService;
import com.conel.market.models.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;

    @Transactional
    public OrderResponse placeOrder(OrderRequest request){
        Order order=Order.builder()
                .status(OrderStatus.PENDING)
                .paymentMethod(request.paymentMethod())
                .shippingAddress(request.shippingAddress())
                .user(User.builder().id(request.userId()).build())
                .orderItems(new ArrayList<>())
                .totalAmount(0.0)
                .build();

        double runningTotalAmount=0.0;
        List<OrderItemResponse> responseItemsList=new ArrayList<>();

        for (OrderItemRequest itemDto:request.items()){
            Product product=productService.getProductEntity(itemDto.productId());

            if (!product.isActive()) {
                throw new IllegalArgumentException("Cannot purchase archived product: " + product.getName());
            }
            productService.decreaseStock(itemDto.productId(),itemDto.quantity());

            double itemPriceAtPurchase = product.getPrice();
            double itemSubTotal = itemPriceAtPurchase * itemDto.quantity();
            runningTotalAmount += itemSubTotal;

            OrderItem orderItem=OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemDto.quantity())
                    .priceAtPurchase(itemPriceAtPurchase)
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
        Order saveOrder=orderRepository.save(order);

        return new OrderResponse(
                saveOrder.getId(),
                saveOrder.getTotalAmount(),
                saveOrder.getStatus().name(),
                saveOrder.getPaymentMethod(),
                saveOrder.getShippingAddress(),
                responseItemsList
        );
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId){
        Order order=orderRepository.findById(orderId)
                .orElseThrow(()->new EntityNotFoundException("Order not found with ID: " + orderId));
        List<OrderItemResponse> itemResponses=order.getOrderItems().stream()
                .map(item->new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getPriceAtPurchase(),
                        item.getQuantity(),
                        item.getPriceAtPurchase()*item.getQuantity()
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
}
