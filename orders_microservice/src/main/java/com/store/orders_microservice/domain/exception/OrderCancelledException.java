package com.store.orders_microservice.domain.exception;

import java.util.UUID;
import com.store.orders_microservice.domain.model.OrderStatus;

public class OrderCancelledException extends OrderDomainException {

    public OrderCancelledException(UUID orderId, OrderStatus currentStatus) {
        super(String.format(
            "Order '%s' is cancelled and cannot be modified or processed further. Current status: '%s'.",
            orderId, currentStatus),
            orderId, currentStatus
        );
    }
}
