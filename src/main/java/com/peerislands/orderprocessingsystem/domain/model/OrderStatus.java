package com.peerislands.orderprocessingsystem.domain.model;

import java.util.EnumSet;

/**
 * Represents all of the supported lifecycle states for an order.
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private static final EnumSet<OrderStatus> TERMINAL_STATUSES = EnumSet.of(DELIVERED, CANCELLED);

    /**
     * Validates whether the transition from the current status to the target status is allowed.
     *
     * @param target the desired target status
     * @return {@code true} when the transition is valid; otherwise {@code false}
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (target == null || this == target) {
            return false;
        }

        if (TERMINAL_STATUSES.contains(this)) {
            // Terminal states cannot transition to any other status.
            return false;
        }

        return switch (this) {
            case PENDING -> target == PROCESSING || target == CANCELLED;
            case PROCESSING -> target == SHIPPED;
            case SHIPPED -> target == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public boolean isTerminal() {
        return TERMINAL_STATUSES.contains(this);
    }
}

