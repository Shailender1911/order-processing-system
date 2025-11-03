package com.peerislands.orderprocessingsystem.service.command;

import java.util.List;

public record CreateOrderCommand(
    String customerName,
    String customerEmail,
    String shippingAddress,
    List<CreateOrderItemCommand> items
) {
}

