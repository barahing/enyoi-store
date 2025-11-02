package com.store.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;

// Usaremos un Java Record para mantener la consistencia con tus eventos
public record ProductStockDTO(
    @JsonProperty("productId") UUID productId,
    @JsonProperty("quantity") int quantity
) implements Serializable {}