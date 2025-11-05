package com.peerislands.orderprocessingsystem.web.controller;

import com.peerislands.orderprocessingsystem.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/tools/orders")
public class OrderMaintenanceController {

    private final OrderService orderService;

    public OrderMaintenanceController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/promote-pending")
    public ResponseEntity<PromotionResponse> promotePendingOrders() {
        int promoted = orderService.promotePendingOrders();
        return ResponseEntity.ok(new PromotionResponse(promoted));
    }

    public record PromotionResponse(int promotedCount) {
    }
}

