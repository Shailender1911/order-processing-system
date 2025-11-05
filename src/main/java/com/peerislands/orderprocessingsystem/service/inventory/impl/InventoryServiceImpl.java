package com.peerislands.orderprocessingsystem.service.inventory.impl;

import com.peerislands.orderprocessingsystem.domain.exception.ResourceNotFoundException;
import com.peerislands.orderprocessingsystem.domain.model.InventoryItem;
import com.peerislands.orderprocessingsystem.domain.model.Order;
import com.peerislands.orderprocessingsystem.domain.model.OrderItem;
import com.peerislands.orderprocessingsystem.repository.InventoryRepository;
import com.peerislands.orderprocessingsystem.service.command.CreateOrderItemCommand;
import com.peerislands.orderprocessingsystem.service.inventory.InventoryService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.MANDATORY)
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void reserveItems(List<CreateOrderItemCommand> items) {
        for (CreateOrderItemCommand item : items) {
            InventoryItem inventoryItem = findInventoryForUpdate(item.productCode());
            inventoryItem.reserve(item.quantity());
        }
    }

    @Override
    public void releaseReservations(Order order) {
        for (OrderItem orderItem : order.getItems()) {
            InventoryItem inventoryItem = findInventoryForUpdate(orderItem.getProductCode());
            inventoryItem.release(orderItem.getQuantity());
        }
    }

    @Override
    public void commitReservations(Order order) {
        for (OrderItem orderItem : order.getItems()) {
            InventoryItem inventoryItem = findInventoryForUpdate(orderItem.getProductCode());
            inventoryItem.commit(orderItem.getQuantity());
        }
    }

    private InventoryItem findInventoryForUpdate(String productCode) {
        return inventoryRepository.findByProductCodeForUpdate(productCode)
            .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product %s".formatted(productCode)));
    }
}

