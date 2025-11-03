package com.peerislands.orderprocessingsystem.web.mapper;

import com.peerislands.orderprocessingsystem.domain.model.Order;
import com.peerislands.orderprocessingsystem.domain.model.OrderItem;
import com.peerislands.orderprocessingsystem.service.command.CreateOrderCommand;
import com.peerislands.orderprocessingsystem.service.command.CreateOrderItemCommand;
import com.peerislands.orderprocessingsystem.web.dto.CreateOrderRequest;
import com.peerislands.orderprocessingsystem.web.dto.OrderItemResponse;
import com.peerislands.orderprocessingsystem.web.dto.OrderResponse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public CreateOrderCommand toCommand(CreateOrderRequest request) {
        Objects.requireNonNull(request, "CreateOrderRequest must not be null");
        List<CreateOrderItemCommand> itemCommands = request.items().stream()
            .map(item -> new CreateOrderItemCommand(item.productCode(), item.productName(), item.quantity(), item.unitPrice()))
            .collect(Collectors.toList());
        return new CreateOrderCommand(request.customerName(), request.customerEmail(), request.shippingAddress(), itemCommands);
    }

    public OrderResponse toResponse(Order order) {
        Objects.requireNonNull(order, "Order must not be null");
        List<OrderItemResponse> itemResponses = order.getItems().stream()
            .map(this::toOrderItemResponse)
            .toList();
        return new OrderResponse(
            order.getId(),
            order.getCustomerName(),
            order.getCustomerEmail(),
            order.getShippingAddress(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            itemResponses
        );
    }

    public List<OrderResponse> toResponse(List<Order> orders) {
        return orders.stream().map(this::toResponse).toList();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return new OrderItemResponse(
            item.getId(),
            item.getProductCode(),
            item.getProductName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getLineTotal()
        );
    }
}

