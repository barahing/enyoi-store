package com.store.orders_microservice.domain.exception;

import java.util.UUID;
import com.store.orders_microservice.domain.model.OrderStatus;

public class InvalidOrderException extends OrderDomainException {

    public InvalidOrderException(UUID orderId, OrderStatus currentStatus, String details) {
        super(String.format(
            "Invalid order '%s': %s (current status: '%s')",
            orderId,
            details != null ? details : "Order validation failed",
            currentStatus
        ), orderId, currentStatus);
    }
}
