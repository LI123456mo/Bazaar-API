package com.conel.market.controller;

import com.conel.market.service.order.OrderService;
import com.conel.market.dto.order.request.OrderRequest;
import com.conel.market.dto.order.response.OrderResponse;
import com.conel.market.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAuthority('order:create')")
    public ResponseEntity<OrderResponse> checkout(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal User authenticatedUser
    ){
        OrderResponse response = orderService.placeOrder(request,authenticatedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('order:read') or hasAuthority('order:read_all')") // allow admins with read_all
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        OrderResponse response = orderService.getOrderById(id,authenticatedUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('order:read_all')")
    public ResponseEntity<Page<OrderResponse>> getAllOrders(
            @PageableDefault(size = 20,sort = "createdAt",direction = Sort.Direction.DESC)Pageable pageable
            ){
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }
}
