package com.store.carts_microservice.domain.factory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.model.CartStatus;

public class CartFactory {

    private CartFactory() {}

    public static Cart createNewCart(UUID clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        return new Cart(
            UUID.randomUUID(), 
            clientId,
            Collections.<CartItem>emptyList(), 
            BigDecimal.ZERO,
            CartStatus.ACTIVE, 
            now,
            now
        );
    }
}