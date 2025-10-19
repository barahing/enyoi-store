package com.store.carts_microservice.domain.exception;

public class CartCreationException extends CartDomainException {

    public CartCreationException(String message) {
        super(message, null, null);
    }

    public static CartCreationException missingClientId() {
        return new CartCreationException("Client ID cannot be null");
    }

    public static CartCreationException emptyItems() {
        return new CartCreationException("Cart must contain at least one item");
    }
}
