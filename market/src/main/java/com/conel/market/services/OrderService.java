package com.conel.market.services;

import com.conel.market.dto.OrderDto;
import com.conel.market.dto.OrderItemDto;
import com.conel.market.dto.OrderResponseDto;
import com.conel.market.mapper.OrderMapper;
import com.conel.market.models.*;
import com.conel.market.repositories.OrderItemRepository;
import com.conel.market.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService{
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final UserService userService;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponseDto createOrder(OrderDto dto){
        //GET USER WHO IS BUYING
        User user=userService.getUserEntity(dto.userId());

        Order order=Order.builder()
                .user(user)
                .totalAmount(0.0)
                .status(OrderStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .createdBy("system")
                .build();

        //SAVE , SO THERE IS ID TO LINK TO ITEMS
        Order savedOrder=orderRepository.save(order);

        double finalTotal=0.0;

        for(OrderItemDto itemDto: dto.items()){
            //FIND THE PRODUCT FOR THIS SPECIFIC ITEM
            Product product=productService.getProductEntity(itemDto.productId());

            //CHECK STOCK AND DECREASE IT
            productService.decreaseStock(product.getId(),itemDto.quantity());

            //CREATE THE LINE ITEM FOR RECEIPT
            OrderItem orderItem=OrderItem.builder()
                    .order(savedOrder)
                    .product(product)
                    .quantity(itemDto.quantity())
                    .priceAtPurchase(product.getPrice())
                    .createdAt(LocalDateTime.now())
                    .createdBy("system")
                    .build();

            //SAVE THE LINE ITEM
            OrderItem savedItem =orderItemRepository.save(orderItem);
            savedOrder.getOrderItems().add(savedItem);

            //ADD  ITEM COST TO RUNNING TOTAL
            finalTotal+=(product.getPrice()*itemDto.quantity());
        }
        //UPDATE TOTAL AMOUNT ON THE MAIN ORDER
        savedOrder.setTotalAmount(finalTotal);
        orderRepository.save(savedOrder);

        //FULL RECEIPT
        return orderMapper.toOrderResponseDto(savedOrder);
    }

    public OrderResponseDto findById(Integer id){
        return orderRepository.findById(id)
                .map(orderMapper::toOrderResponseDto)
                .orElseThrow(()->new RuntimeException("Category not found with id:" + id));
    }

    public List<OrderResponseDto> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toOrderResponseDto)
                .toList();
    }
}