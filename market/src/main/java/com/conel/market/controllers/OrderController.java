package com.conel.market.controllers;

import com.conel.market.dto.OrderDto;
import com.conel.market.dto.OrderResponseDto;
import com.conel.market.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public OrderResponseDto create(@RequestBody OrderDto dto){
        return orderService.createOrder(dto);
    }
    @GetMapping("/{id}")
    public OrderResponseDto findById(@PathVariable Integer id) {
        return orderService.findById(id);
    }
    @GetMapping List<OrderResponseDto> findAll(){
        return orderService.findAll();
    }
}
