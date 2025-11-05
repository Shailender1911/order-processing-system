package com.peerislands.orderprocessingsystem.service.inventory;

import com.peerislands.orderprocessingsystem.domain.model.Order;
import com.peerislands.orderprocessingsystem.service.command.CreateOrderItemCommand;
import java.util.List;

public interface InventoryService {

    void reserveItems(List<CreateOrderItemCommand> items);

    void releaseReservations(Order order);

    void commitReservations(Order order);
}

