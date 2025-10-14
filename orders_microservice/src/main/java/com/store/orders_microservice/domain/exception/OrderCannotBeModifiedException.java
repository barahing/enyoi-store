package com.store.orders_microservice.domain.exception;

import java.util.UUID;
import com.store.orders_microservice.domain.model.OrderStatus;

public class OrderCannotBeModifiedException extends OrderDomainException {

    public OrderCannotBeModifiedException(UUID orderId, OrderStatus currentStatus) {
        super(String.format(
            "Order '%s' cannot be modified because its current status is '%s'. Only orders in 'CREATED' status can be modified or cancelled.",
            orderId, currentStatus),
            orderId, currentStatus
        );
    }
}
