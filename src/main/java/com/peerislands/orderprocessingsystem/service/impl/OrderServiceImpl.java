package com.peerislands.orderprocessingsystem.service.impl;

import com.peerislands.orderprocessingsystem.domain.exception.InvalidOrderStateException;
import com.peerislands.orderprocessingsystem.domain.exception.OrderNotFoundException;
import com.peerislands.orderprocessingsystem.domain.model.Order;
import com.peerislands.orderprocessingsystem.domain.model.OrderItem;
import com.peerislands.orderprocessingsystem.domain.model.OrderStatus;
import com.peerislands.orderprocessingsystem.repository.OrderRepository;
import com.peerislands.orderprocessingsystem.service.OrderService;
import com.peerislands.orderprocessingsystem.service.command.CreateOrderCommand;
import com.peerislands.orderprocessingsystem.service.command.CreateOrderItemCommand;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public Order createOrder(CreateOrderCommand command) {
        Objects.requireNonNull(command, "CreateOrderCommand must not be null");
        validateOrderDetails(command);
        validateItems(command.items());

        Order order = new Order(command.customerName(), command.customerEmail(), command.shippingAddress());
        command.items().forEach(itemCommand -> order.addItem(toOrderItem(itemCommand)));

        return orderRepository.save(order);
    }

    private void validateOrderDetails(CreateOrderCommand command) {
        if (isNullOrEmpty(command.customerName())) {
            throw new InvalidOrderStateException("Customer name is required");
        }
        if (isNullOrEmpty(command.customerEmail())) {
            throw new InvalidOrderStateException("Customer email is required");
        }
        if (isNullOrEmpty(command.shippingAddress())) {
            throw new InvalidOrderStateException("Shipping address is required");
        }
    }

    private void validateItems(List<CreateOrderItemCommand> items) {
        if (items == null || items.isEmpty()) {
            throw new InvalidOrderStateException("Order must contain at least one item");
        }

        items.forEach(item -> {
            if (item.quantity() <= 0) {
                throw new InvalidOrderStateException("Item quantity must be greater than zero");
            }
            if (isNullOrEmpty(item.productCode()) || isNullOrEmpty(item.productName())) {
                throw new InvalidOrderStateException("Product code and name are required for each item");
            }
            if (item.unitPrice() == null || item.unitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidOrderStateException("Unit price must be greater than zero");
            }
        });
    }

    private boolean isNullOrEmpty(String value) {
        return value == null || value.isBlank();
    }

    private OrderItem toOrderItem(CreateOrderItemCommand command) {
        return new OrderItem(command.productCode(), command.productName(), command.quantity(), command.unitPrice());
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrders(Optional<OrderStatus> statusFilter) {
        return statusFilter.map(orderRepository::findByStatus).orElseGet(orderRepository::findAll);
    }

    @Override
    public Order updateOrderStatus(Long id, OrderStatus targetStatus) {
        Order order = getOrder(id);
        if (targetStatus == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException("Use the cancel endpoint to cancel an order");
        }
        order.updateStatus(targetStatus);
        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long id) {
        Order order = getOrder(id);
        order.cancel();
        return orderRepository.save(order);
    }

    @Override
    public int promotePendingOrders() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        int updatedCount = 0;
        for (Order order : pendingOrders) {
            if (order.markProcessing()) {
                updatedCount++;
            }
        }
        if (updatedCount > 0) {
            orderRepository.saveAll(pendingOrders);
        }
        return updatedCount;
    }
}

