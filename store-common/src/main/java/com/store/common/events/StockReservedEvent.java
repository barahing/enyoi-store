// En: store-common/com/store/common/events/StockReservedEvent.java
package com.store.common.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;

public record StockReservedEvent(
    @JsonProperty("orderId") UUID orderId
) implements Serializable {}