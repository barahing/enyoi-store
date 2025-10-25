package com.store.carts_microservice.domain.factory;

import java.math.BigDecimal;
import java.util.UUID;

import com.store.carts_microservice.domain.model.CartItem;

public class CartItemFactory {

    private CartItemFactory() {}

    public static CartItem create(UUID productId, Integer quantity, BigDecimal price) {
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
        return new CartItem(productId, quantity, price, subtotal);
    }
}