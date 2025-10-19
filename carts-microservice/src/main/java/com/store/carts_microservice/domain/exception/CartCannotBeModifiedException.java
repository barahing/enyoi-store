package com.store.carts_microservice.domain.exception;

import java.util.UUID;
import com.store.carts_microservice.domain.model.CartStatus;

public class CartCannotBeModifiedException extends CartDomainException {
    public CartCannotBeModifiedException(UUID cartId, CartStatus status) {
        super(
            String.format("Cart %s cannot be modified because it is in status %s", cartId, status),
            cartId,
            status
        );
    }
}
