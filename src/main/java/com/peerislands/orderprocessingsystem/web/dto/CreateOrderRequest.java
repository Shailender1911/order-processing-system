package com.peerislands.orderprocessingsystem.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    @NotBlank(message = "Customer name is required") String customerName,
    @NotBlank(message = "Customer email is required") @Email(message = "Customer email must be valid") String customerEmail,
    @NotBlank(message = "Shipping address is required") String shippingAddress,
    @NotEmpty(message = "At least one order item is required") @Valid List<OrderItemRequest> items
) {

    public record OrderItemRequest(
        @NotBlank(message = "Product code is required") String productCode,
        @NotBlank(message = "Product name is required") String productName,
        @Positive(message = "Quantity must be greater than zero") int quantity,
        @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive") BigDecimal unitPrice
    ) {
    }
}

