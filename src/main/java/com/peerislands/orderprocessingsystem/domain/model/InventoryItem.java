package com.peerislands.orderprocessingsystem.domain.model;

import com.peerislands.orderprocessingsystem.domain.exception.InsufficientInventoryException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_code", nullable = false, unique = true, length = 100)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "stock_on_hand", nullable = false)
    private int stockOnHand;

    @Column(name = "reserved_quantity", nullable = false)
    private int reservedQuantity;

    @Version
    private long version;

    protected InventoryItem() {
        // JPA
    }

    public InventoryItem(String productCode, String productName, int stockOnHand) {
        this.productCode = productCode;
        this.productName = productName;
        this.stockOnHand = stockOnHand;
    }

    public void reserve(int quantity) {
        ensurePositive(quantity);
        if (getAvailableQuantity() < quantity) {
            throw new InsufficientInventoryException(productCode, quantity, getAvailableQuantity());
        }
        reservedQuantity += quantity;
    }

    public void release(int quantity) {
        ensurePositive(quantity);
        if (quantity > reservedQuantity) {
            throw new IllegalStateException("Cannot release more than reserved for product %s".formatted(productCode));
        }
        reservedQuantity -= quantity;
    }

    public void commit(int quantity) {
        ensurePositive(quantity);
        if (quantity > reservedQuantity) {
            throw new IllegalStateException("Cannot commit more than reserved for product %s".formatted(productCode));
        }
        reservedQuantity -= quantity;
        stockOnHand -= quantity;
    }

    public int getAvailableQuantity() {
        return stockOnHand - reservedQuantity;
    }

    private void ensurePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public int getStockOnHand() {
        return stockOnHand;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }
}

