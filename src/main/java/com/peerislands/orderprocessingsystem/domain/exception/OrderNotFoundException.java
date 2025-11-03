package com.peerislands.orderprocessingsystem.domain.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order %d not found".formatted(orderId));
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}

