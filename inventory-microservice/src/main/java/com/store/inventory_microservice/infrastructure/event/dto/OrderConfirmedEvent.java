package com.store.inventory_microservice.infrastructure.event.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Value;

@Value
public class OrderConfirmedEvent {
    UUID orderId;
    UUID clientId;
    LocalDateTime createdDate;
    List<OrderItemEventDto> items;
}