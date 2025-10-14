package com.store.orders_microservice.domain.exception;

import java.util.UUID;
import com.store.orders_microservice.domain.model.OrderStatus;

public class OrderCannotBeShippedException extends OrderDomainException {

    public OrderCannotBeShippedException(UUID orderId, OrderStatus currentStatus) {
        super(String.format(
            "Order '%s' cannot be shipped because its current status is '%s'. Only orders in 'CONFIRMED' status can be shipped.",
            orderId, currentStatus),
            orderId, currentStatus
        );
    }
}
