package com.store.orders_microservice.domain.exception;

import java.util.UUID;
import com.store.orders_microservice.domain.model.OrderStatus;

public abstract class OrderDomainException extends RuntimeException {
    protected final UUID orderId;
    protected final OrderStatus currentStatus;

    protected OrderDomainException(String message, UUID orderId, OrderStatus currentStatus) {
        super(message);
        this.orderId = orderId;
        this.currentStatus = currentStatus;
    }

    public UUID getOrderId() { 
        return orderId; 
    }
    
    public OrderStatus getCurrentStatus() { 
        return currentStatus; 
    }
}
