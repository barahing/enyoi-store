package com.store.common.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;

public record ProductStockDTO(
    @JsonProperty("productId") UUID productId,
    @JsonProperty("quantity") int quantity
) implements Serializable {}
