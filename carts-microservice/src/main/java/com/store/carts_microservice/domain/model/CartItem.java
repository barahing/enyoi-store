package com.store.carts_microservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItem(
    UUID productId,
    Integer quantity,
    BigDecimal price,
    BigDecimal subtotal
) {
    public static CartItem create(UUID productId, Integer quantity, BigDecimal price) {
        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(quantity));
        return new CartItem(productId, quantity, price, subtotal);
    }

    public CartItem withUpdatedQuantity(int newQuantity) {
        BigDecimal newSubtotal = price.multiply(BigDecimal.valueOf(newQuantity));
        return new CartItem(productId, newQuantity, price, newSubtotal);
    }
}
