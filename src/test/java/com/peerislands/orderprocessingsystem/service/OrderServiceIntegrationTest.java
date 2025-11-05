package com.peerislands.orderprocessingsystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.peerislands.orderprocessingsystem.domain.exception.InsufficientInventoryException;
import com.peerislands.orderprocessingsystem.domain.exception.InvalidOrderStateException;
import com.peerislands.orderprocessingsystem.domain.model.InventoryItem;
import com.peerislands.orderprocessingsystem.domain.model.Order;
import com.peerislands.orderprocessingsystem.domain.model.OrderStatus;
import com.peerislands.orderprocessingsystem.repository.InventoryRepository;
import com.peerislands.orderprocessingsystem.repository.OrderRepository;
import com.peerislands.orderprocessingsystem.service.command.CreateOrderCommand;
import com.peerislands.orderprocessingsystem.service.command.CreateOrderItemCommand;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("h2")
class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        inventoryRepository.deleteAll();
        inventoryRepository.save(new InventoryItem("SKU-123", "Wireless Mouse", 10));
        inventoryRepository.save(new InventoryItem("SKU-999", "Mechanical Keyboard", 5));
    }

    @Test
    void createOrder_persistsAndReturnsOrder() {
        Order order = orderService.createOrder(sampleCommand());

        assertThat(order.getId()).isNotNull();
        assertThat(order.getOrderNumber()).isNotBlank().startsWith("ORD-");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getTotalAmount()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(order.getItems()).hasSize(2);
        assertThat(inventoryRepository.findByProductCode("SKU-123").orElseThrow().getReservedQuantity())
            .isEqualTo(1);
        assertThat(inventoryRepository.findByProductCode("SKU-999").orElseThrow().getReservedQuantity())
            .isEqualTo(1);
    }

    @Test
    void updateOrderStatus_allowsValidTransition() {
        Order order = orderService.createOrder(sampleCommand());

        Order updated = orderService.updateOrderStatus(order.getId(), OrderStatus.PROCESSING);

        assertThat(updated.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(inventoryRepository.findByProductCode("SKU-123").orElseThrow().getReservedQuantity())
            .isZero();
        assertThat(inventoryRepository.findByProductCode("SKU-123").orElseThrow().getStockOnHand())
            .isEqualTo(9);
    }

    @Test
    void updateOrderStatus_rejectsInvalidTransition() {
        Order order = orderService.createOrder(sampleCommand());

        assertThatThrownBy(() -> orderService.updateOrderStatus(order.getId(), OrderStatus.DELIVERED))
            .isInstanceOf(InvalidOrderStateException.class);
    }

    @Test
    void cancelOrder_marksOrderAsCancelled() {
        Order order = orderService.createOrder(sampleCommand());

        Order cancelled = orderService.cancelOrder(order.getId());

        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(inventoryRepository.findByProductCode("SKU-123").orElseThrow().getReservedQuantity())
            .isZero();
        assertThat(inventoryRepository.findByProductCode("SKU-123").orElseThrow().getStockOnHand())
            .isEqualTo(10);
    }

    @Test
    void promotePendingOrders_transitionsPendingOrdersToProcessing() {
        Order order = orderService.createOrder(sampleCommand());

        int updated = orderService.promotePendingOrders();

        Order reloaded = orderService.getOrder(order.getId());
        assertThat(reloaded.getOrderNumber()).isEqualTo(order.getOrderNumber());
        assertThat(updated).isEqualTo(1);
        assertThat(reloaded.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        assertThat(inventoryRepository.findByProductCode("SKU-123").orElseThrow().getStockOnHand())
            .isEqualTo(9);
        assertThat(inventoryRepository.findByProductCode("SKU-123").orElseThrow().getReservedQuantity())
            .isZero();
    }

    @Test
    void createOrder_throwsWhenInventoryInsufficient() {
        CreateOrderCommand command = new CreateOrderCommand(
            "Jane Doe",
            "jane.doe@example.com",
            "221B Baker Street, London",
            List.of(new CreateOrderItemCommand("SKU-999", "Mechanical Keyboard", 99, new BigDecimal("25.00")))
        );

        assertThatThrownBy(() -> orderService.createOrder(command))
            .isInstanceOf(InsufficientInventoryException.class);
    }

    private CreateOrderCommand sampleCommand() {
        return new CreateOrderCommand(
            "Jane Doe",
            "jane.doe@example.com",
            "221B Baker Street, London",
            List.of(
                new CreateOrderItemCommand("SKU-123", "Wireless Mouse", 1, new BigDecimal("15.00")),
                new CreateOrderItemCommand("SKU-999", "Mechanical Keyboard", 1, new BigDecimal("25.00"))
            )
        );
    }
}

