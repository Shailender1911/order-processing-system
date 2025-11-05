package com.peerislands.orderprocessingsystem.service;

import com.peerislands.orderprocessingsystem.domain.model.Order;
import com.peerislands.orderprocessingsystem.domain.model.OrderStatus;
import com.peerislands.orderprocessingsystem.service.command.CreateOrderCommand;
import java.util.List;
import java.util.Optional;

public interface OrderService {

    Order createOrder(CreateOrderCommand command);

    Order getOrder(String orderNumber);

    List<Order> getOrders(Optional<OrderStatus> statusFilter);

    Order updateOrderStatus(String orderNumber, OrderStatus targetStatus);

    Order cancelOrder(String orderNumber);

    int promotePendingOrders();
}

