package com.conel.market.models.order;

import com.conel.market.models.order.dto.request.OrderRequest;
import com.conel.market.models.order.dto.response.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")// Only logged-in shoppers can check out
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody OrderRequest request){
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()") // Ensures transaction receipts are protected
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("id") String id) {
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }
}
