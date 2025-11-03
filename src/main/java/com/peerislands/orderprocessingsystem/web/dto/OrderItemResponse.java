package com.peerislands.orderprocessingsystem.web.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    String productCode,
    String productName,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {
}

