package com.store.inventory_microservice.domain.exception;

import java.util.UUID;

public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException(UUID productId, int available, int requested) {
        super(String.format("Product %s has insufficient stock. Available: %d, Requested: %d.", 
            productId, available, requested));
    }
}