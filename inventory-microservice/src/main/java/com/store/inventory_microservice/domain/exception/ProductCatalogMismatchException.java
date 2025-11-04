package com.store.inventory_microservice.domain.exception;

import java.util.UUID;

public class ProductCatalogMismatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    public ProductCatalogMismatchException(UUID productId, String reason) {
        super("Product ID " + productId + " not found in catalog. Reason: " + reason);
    }

    public ProductCatalogMismatchException(String message) {
        super(message);
    }
}
