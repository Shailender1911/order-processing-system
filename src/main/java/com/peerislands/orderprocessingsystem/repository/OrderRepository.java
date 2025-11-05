package com.peerislands.orderprocessingsystem.repository;

import com.peerislands.orderprocessingsystem.domain.model.Order;
import com.peerislands.orderprocessingsystem.domain.model.OrderStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = "items")
    List<Order> findAll();

    @EntityGraph(attributePaths = "items")
    List<Order> findByStatus(OrderStatus status);

    boolean existsByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = "items")
    Optional<Order> findByOrderNumber(String orderNumber);
}

