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
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public OrderResponse placeOrder(OrderRequest request,String authenticatedUserId) {
        User buyer = userRepository.findById(authenticatedUserId)
                .orElseThrow(()->new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<Product> products=new ArrayList<>();

        for(OrderItemRequest itemDto:request.items()){
            Product product=productService.getProductEntity(itemDto.productId());

            if (!product.isActive()) {
                throw new BusinessException(ErrorCode.PRODUCT_ARCHIVED);
            }

            if (product.getStockQuantity() < itemDto.quantity()) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
            }

            products.add(product);
        }

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

        int index=0;
        for (OrderItemRequest itemDto : request.items()) {
            Product product = products.get(index++);

            Product lockeProduct=productService.getProductEntityWithLock(product.getId());

            if (lockeProduct.getStockQuantity()<itemDto.quantity()){
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
            }

            lockeProduct.setStockQuantity(lockeProduct.getStockQuantity()- itemDto.quantity());
            productService.saveProduct(lockeProduct);


            double itemPriceAtPurchase = lockeProduct.getPrice();
            double itemSubTotal = itemPriceAtPurchase * itemDto.quantity();
            runningTotalAmount += itemSubTotal;

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(lockeProduct)
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

    @Transactional(readOnly = true)
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


    /**
     * Validate that the currently authenticated user is allowed to view this order.
     * Allows access if:
     *  - the order belongs to the authenticated user
     *  - OR the current authentication has authority "order:read_all" (admin override)
     *
     * Throws BusinessException(ErrorCode.ACCESS_DENIED) if not allowed.
     */
    public void validateOwnership(Order order,String authenticatedUserId){
        // Check for admin-level authority
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> "order:read_all".equals(a.getAuthority()))) {
            // Has admin-level permission to read any order
            return;
        }

        // Otherwise require that the owner matches
        if (!order.getUser().getId().equals(authenticatedUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }
}