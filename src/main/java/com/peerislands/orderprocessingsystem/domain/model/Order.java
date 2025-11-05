package com.peerislands.orderprocessingsystem.domain.model;

import com.peerislands.orderprocessingsystem.domain.exception.InvalidOrderStateException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 180)
    private String customerEmail;

    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<OrderItem> items = new ArrayList<>();

    protected Order() {
        // JPA requirement
    }

    public Order(String orderNumber, String customerName, String customerEmail, String shippingAddress) {
        this.orderNumber = Objects.requireNonNull(orderNumber, "orderNumber");
        this.customerName = Objects.requireNonNull(customerName, "customerName");
        this.customerEmail = Objects.requireNonNull(customerEmail, "customerEmail");
        this.shippingAddress = Objects.requireNonNull(shippingAddress, "shippingAddress");
    }

    public void addItem(OrderItem item) {
        Objects.requireNonNull(item, "item");
        item.assignOrder(this);
        items.add(item);
        recalculateTotal();
    }

    public void updateStatus(OrderStatus targetStatus) {
        if (targetStatus == null) {
            throw new InvalidOrderStateException("Target order status must be provided");
        }
        if (status == targetStatus) {
            return;
        }
        if (!status.canTransitionTo(targetStatus)) {
            throw new InvalidOrderStateException(
                "Cannot transition order %d from %s to %s".formatted(id, status, targetStatus)
            );
        }
        this.status = targetStatus;
    }

    public void cancel() {
        if (status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only pending orders can be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    public boolean markProcessing() {
        if (this.status == OrderStatus.PENDING) {
            this.status = OrderStatus.PROCESSING;
            return true;
        }
        return false;
    }

    private void recalculateTotal() {
        this.totalAmount = items.stream()
            .map(OrderItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

}

