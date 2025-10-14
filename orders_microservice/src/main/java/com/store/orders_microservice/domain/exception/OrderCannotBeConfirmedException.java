package com.store.orders_microservice.domain.exception;

import java.util.UUID;
import com.store.orders_microservice.domain.model.OrderStatus;

public class OrderCannotBeConfirmedException extends OrderDomainException {

    public OrderCannotBeConfirmedException(UUID orderId, OrderStatus currentStatus) {
        super(String.format(
            "Order '%s' cannot be confirmed because its current status is '%s'. Only orders in 'CREATED' status can be confirmed.",
            orderId, currentStatus),
            orderId, currentStatus
        );
    }
}
