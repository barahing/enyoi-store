package com.store.products_microservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Product(
    UUID id,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    UUID categoryId
) {

    public Product withStock(Integer newStock) {
        if (newStock == null || newStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative or empty");
        }
        return new Product(id, name, description, price, newStock, categoryId);
    }

    public Product increaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Increase amount must be higher than 0");
        }
        return withStock(this.stock + amount);
    }

    public Product decreaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Decrease amount must be higher than 0");
        }
        if (this.stock < amount) {
            throw new IllegalArgumentException("Not enough stock");
        }
        return withStock(this.stock - amount);
    }
}
