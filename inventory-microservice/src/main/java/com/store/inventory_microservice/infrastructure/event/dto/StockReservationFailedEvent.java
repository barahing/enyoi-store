package com.store.inventory_microservice.infrastructure.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockReservationFailedEvent(
    UUID orderId,
    String reason,
    LocalDateTime failedDate,
    String message
) {
    public StockReservationFailedEvent(UUID orderId, String reason) {
        this(
            orderId, 
            reason, 
            LocalDateTime.now(), 
            "Stock reservation failed. Order must be cancelled and compensated."
        );
    }
}