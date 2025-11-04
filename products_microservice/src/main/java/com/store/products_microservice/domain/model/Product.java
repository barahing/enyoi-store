package com.store.products_microservice.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public record Product(
    UUID id,
    String name,
    String description,
    BigDecimal price,
    UUID categoryId
) {}
