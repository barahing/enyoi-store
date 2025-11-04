package com.store.inventory_microservice.domain.exception;

import java.util.UUID;

public class ProductAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public ProductAlreadyExistsException(UUID productId) {
        super("Stock already exists for Product ID: " + productId);
    }

    public ProductAlreadyExistsException(String message) {
        super(message);
    }
}
