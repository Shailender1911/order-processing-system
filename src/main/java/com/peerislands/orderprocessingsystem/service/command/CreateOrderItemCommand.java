package com.peerislands.orderprocessingsystem.service.command;

import java.math.BigDecimal;

public record CreateOrderItemCommand(
    String productCode,
    String productName,
    int quantity,
    BigDecimal unitPrice
) {
}

