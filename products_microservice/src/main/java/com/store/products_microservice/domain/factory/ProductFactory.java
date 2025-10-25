package com.store.products_microservice.domain.factory;

import java.math.BigDecimal;
import java.util.UUID;
import com.store.products_microservice.domain.model.Product;

public class ProductFactory {
    private ProductFactory() {}

    public static Product createNew(String name, String description, BigDecimal price, Integer initialStock, UUID categoryId) {
        
        if (initialStock == null || initialStock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative or empty");
        }

        return new Product(
            null,
            name,
            description,
            price,
            initialStock,
            0,
            categoryId
        );
    }
}