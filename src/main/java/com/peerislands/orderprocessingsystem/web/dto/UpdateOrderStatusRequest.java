package com.peerislands.orderprocessingsystem.web.dto;

import com.peerislands.orderprocessingsystem.domain.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
    @NotNull(message = "Status must be provided") OrderStatus status
) {
}

