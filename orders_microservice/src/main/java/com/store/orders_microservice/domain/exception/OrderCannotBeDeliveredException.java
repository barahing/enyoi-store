package com.store.orders_microservice.domain.exception;

import java.util.UUID;
import com.store.orders_microservice.domain.model.OrderStatus;

public class OrderCannotBeDeliveredException extends OrderDomainException {

    public OrderCannotBeDeliveredException(UUID orderId, OrderStatus currentStatus) {
        super(String.format(
            "Order '%s' cannot be delivered because its current status is '%s'. Only orders in 'SHIPPED' status can be delivered.",
            orderId, currentStatus),
            orderId, currentStatus
        );
    }
}
