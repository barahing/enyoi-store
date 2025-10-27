package com.store.inventory_microservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Setter
public class ProductStock {

    private UUID productId;
    private int currentStock;
    private int reservedStock;
    private LocalDateTime updatedAt;

    private ProductStock(UUID productId, int currentStock, int reservedStock) {
        this.productId = productId;
        this.currentStock = currentStock;
        this.reservedStock = reservedStock;
    }

    public static ProductStock createNew(UUID productId, int initialStock) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("Initial stock cannot be negative.");
        }
        return new ProductStock(productId, initialStock, 0);
    }
    
    public static ProductStock fromPersistence(UUID productId, int currentStock, int reservedStock) {
        return new ProductStock(productId, currentStock, reservedStock);
    }

    public void reserveStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        
        int available = this.currentStock - this.reservedStock;
        
        if (available < quantity) {
            throw new IllegalStateException(
                String.format("Not enough stock for product %s. Available: %d, Requested: %d", 
                productId, available, quantity)
            );
        }
        this.reservedStock += quantity;
    }

    public void deductReservedStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        
        if (this.reservedStock < quantity) {
            throw new IllegalStateException("Cannot deduct more reserved stock than currently reserved.");
        }
        
        this.currentStock -= quantity;
        this.reservedStock -= quantity;
    }
    
    public void releaseReservedStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        
        if (this.reservedStock < quantity) {
            throw new IllegalStateException("Cannot release more reserved stock than currently reserved.");
        }
        
        this.reservedStock -= quantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public int getReservedStock() {
        return reservedStock;
    }
    
    public int getAvailableStock() {
        return this.currentStock - this.reservedStock;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}