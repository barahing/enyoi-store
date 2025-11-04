package com.store.inventory_microservice.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Builder;

@Builder
public record ProductDto(
    UUID productId,
    String name,
    String description,
    BigDecimal price,
    Integer stock
) {}
