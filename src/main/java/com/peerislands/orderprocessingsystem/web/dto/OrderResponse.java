package com.peerislands.orderprocessingsystem.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.peerislands.orderprocessingsystem.domain.model.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OrderResponse(
    Long id,
    String orderNumber,
    String customerName,
    String customerEmail,
    String shippingAddress,
    OrderStatus status,
    BigDecimal totalAmount,
    Instant createdAt,
    Instant updatedAt,
    List<OrderItemResponse> items
) {
}

