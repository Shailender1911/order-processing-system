package com.peerislands.orderprocessingsystem.scheduler;

import com.peerislands.orderprocessingsystem.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusScheduler.class);

    private final OrderService orderService;

    public OrderStatusScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void promotePendingOrders() {
        int updated = orderService.promotePendingOrders();
        if (updated > 0) {
            log.info("Promoted {} pending order(s) to PROCESSING status", updated);
        } else {
            log.debug("No pending orders found for promotion");
        }
    }
}

