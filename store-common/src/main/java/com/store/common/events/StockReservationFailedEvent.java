package com.store.common.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record StockReservationFailedEvent(
    @JsonProperty("orderId") UUID orderId,
    @JsonProperty("reason") String reason,
    @JsonProperty("failedProducts") List<ProductStockDTO> failedProducts
) implements Serializable {
    public StockReservationFailedEvent(UUID orderId, String reason) {
        this(orderId, reason, Collections.emptyList()); 
    }
}
