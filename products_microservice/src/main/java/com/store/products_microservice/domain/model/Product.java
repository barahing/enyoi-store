package com.store.products_microservice.domain.model;

import com.store.products_microservice.domain.exception.InconsistentStockException;
import com.store.products_microservice.domain.exception.NotEnoughStockException;
import java.math.BigDecimal;
import java.util.UUID;

public record Product(
    UUID id,
    String name,
    String description,
    BigDecimal price,
    int stockAvailable,
    int stockReserved,
    UUID categoryId
) {
    public boolean canReserve(int amount) {
        return this.stockAvailable >= amount;
    }

    public Product reserveStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to reserve must be positive");
        }
        if (!canReserve(amount)) {
            throw new NotEnoughStockException("Not enough stock available for product: " + id);
        }
        
        return new Product(
            this.id, 
            this.name, 
            this.description, 
            this.price, 
            this.stockAvailable - amount, 
            this.stockReserved + amount,  
            this.categoryId
        );
    }

    public Product releaseStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to release must be positive");
        }
        if (this.stockReserved < amount) {
            throw new InconsistentStockException("Cannot release more stock than reserved for product: " + id);
        }
        
        return new Product(
            this.id,
            this.name,
            this.description,
            this.price,
            this.stockAvailable + amount, 
            this.stockReserved - amount,  
            this.categoryId
        );
    }

    public Product confirmStockSale(int amount) {
         if (amount <= 0) {
            throw new IllegalArgumentException("Amount to confirm must be positive");
        }
        if (this.stockReserved < amount) {
             throw new InconsistentStockException("Cannot confirm sale of more stock than reserved for product: " + id);
        }
        
        return new Product(
            this.id,
            this.name,
            this.description,
            this.price,
            this.stockAvailable, 
            this.stockReserved - amount,  
            this.categoryId
        );
    }
}