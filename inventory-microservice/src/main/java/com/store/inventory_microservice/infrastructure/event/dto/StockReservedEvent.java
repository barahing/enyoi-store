package com.store.inventory_microservice.infrastructure.event.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class StockReservedEvent {
    UUID orderId;
    LocalDateTime reservedDate = LocalDateTime.now();
    String message = "Stock successfully reserved. Order can proceed to CONFIRMED status.";
}