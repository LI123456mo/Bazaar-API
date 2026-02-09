package com.conel.market.controllers;

import com.conel.market.dto.OrderDto;
import com.conel.market.dto.OrderResponseDto;
import com.conel.market.services.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDto> create(@Valid @RequestBody OrderDto dto){
        OrderResponseDto response=orderService.createOrder(dto);
        return ResponseEntity.status(201).body(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> findById(@PathVariable Integer id) {

        OrderResponseDto responseDto=orderService.findById(id);
        return ResponseEntity.ok(responseDto);
    }
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> findAll(){
        List<OrderResponseDto> response=orderService.findAll();
        return ResponseEntity.ok(response);
    }
}
