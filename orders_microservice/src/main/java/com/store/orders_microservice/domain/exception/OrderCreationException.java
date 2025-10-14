package com.store.orders_microservice.domain.exception;

public class OrderCreationException extends OrderDomainException {

    public OrderCreationException(String message) {
        super(message, null, null);
    }

    public static OrderCreationException missingClientId() {
        return new OrderCreationException("Client ID cannot be null");
    }

    public static OrderCreationException emptyItems() {
        return new OrderCreationException("Order must contain at least one item");
    }
}
