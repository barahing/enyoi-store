package com.store.inventory_microservice.infrastructure.event.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Value;

@Value
public class OrderItemEventDto {
    UUID productId;
    int quantity;
    BigDecimal price;
}