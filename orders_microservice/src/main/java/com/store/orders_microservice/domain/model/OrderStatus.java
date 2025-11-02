package com.store.orders_microservice.domain.model;

public enum OrderStatus {
    PENDING,
    PAYMENT_APPROVED,
    STOCK_RESERVED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
