package com.store.orders_microservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItem(
    UUID productId,
    Integer quantity,
    BigDecimal price,
    BigDecimal subtotal
) {

    public static OrderItem create(UUID productId, Integer quantity, BigDecimal price) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
        return new OrderItem(productId, quantity, price, subtotal);
    }

    public OrderItem withUpdatedQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        BigDecimal newSubtotal = this.price.multiply(BigDecimal.valueOf(newQuantity));
        return new OrderItem(this.productId, newQuantity, this.price, newSubtotal);
    }

    public OrderItem withUpdatedPrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        BigDecimal newSubtotal = newPrice.multiply(BigDecimal.valueOf(this.quantity));
        return new OrderItem(this.productId, this.quantity, newPrice, newSubtotal);
    }
}
