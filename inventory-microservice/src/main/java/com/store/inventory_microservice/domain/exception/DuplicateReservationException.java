package com.store.inventory_microservice.domain.exception;

import java.util.UUID;

public class DuplicateReservationException extends RuntimeException {

    public DuplicateReservationException(UUID orderId, UUID productId) {
        super(String.format("Duplicate reservation detected for Order ID %s and Product ID %s",
                orderId, productId));
    }
}
