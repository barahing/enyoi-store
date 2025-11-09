package com.store.inventory_microservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class StockReservation {

    UUID id;
    UUID orderId;
    UUID productId;
    int quantity;
    LocalDateTime reservedAt;

    public static StockReservation create(UUID orderId, UUID productId, Integer quantity) {
        return new StockReservation(
            null,  
            orderId,
            productId, 
            quantity,
            LocalDateTime.now()
        );
    }
}
