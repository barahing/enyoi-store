package com.store.orders_microservice.domain.exception;

import java.util.UUID;

public class OrderNotFoundException extends OrderDomainException {

    public OrderNotFoundException(UUID orderId) {
        super(String.format(
            "Order '%s' not found.",
            orderId),
            orderId, 
            null 
        );
    }
}