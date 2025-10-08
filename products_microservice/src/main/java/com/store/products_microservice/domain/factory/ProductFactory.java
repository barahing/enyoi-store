package com.store.products_microservice.domain.factory;

import java.math.BigDecimal;
import java.util.UUID;

import com.store.products_microservice.domain.model.Product;

public class ProductFactory {
    private ProductFactory() {}

    public static Product createNew(String name, String description, BigDecimal price, Integer stock, UUID categoryId){
        return new Product(
            null,
            name,
            description,
            price,
            stock,
            categoryId
        );
    }
}
