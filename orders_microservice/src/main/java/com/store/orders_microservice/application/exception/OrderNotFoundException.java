package com.store.orders_microservice.application.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {
    
    public OrderNotFoundException(UUID orderId) {
        super("Order not found with id: " + orderId);
    }

    public OrderNotFoundException(String message) {
        super(message);
    }
}
