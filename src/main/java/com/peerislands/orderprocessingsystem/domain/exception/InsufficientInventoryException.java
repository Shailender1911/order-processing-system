package com.peerislands.orderprocessingsystem.domain.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(String productCode, int requested, int available) {
        super("Insufficient inventory for product %s. Requested: %d, Available: %d".formatted(productCode, requested, available));
    }
}

