package com.store.products_microservice.domain.exception;

public class InconsistentStockException extends RuntimeException {
    public InconsistentStockException(String message) {
        super(message);
    }
}