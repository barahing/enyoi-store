package com.store.carts_microservice.domain.exception;

import java.util.UUID;
import com.store.carts_microservice.domain.model.CartStatus;

import lombok.Getter;

@Getter
public abstract class CartDomainException extends RuntimeException {
    protected final UUID cartId;
    protected final CartStatus currentStatus;

    protected CartDomainException(String message, UUID cartId, CartStatus currentStatus) {
        super(message);
        this.cartId = cartId;
        this.currentStatus = currentStatus;
    }

    public UUID getCartId() { 
        return cartId; 
    }
    
    public CartStatus getCurrentStatus() { 
        return currentStatus; 
    }
}
