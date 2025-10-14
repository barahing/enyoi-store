package com.store.products_microservice.domain.model;

import java.util.UUID;

public record Category(
    UUID id,
    String name,
    String description
) {}

