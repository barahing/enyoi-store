// En: store-common/com/store/common/events/ReleaseStockCommand.java
package com.store.common.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record ReleaseStockCommand(
    @JsonProperty("orderId") UUID orderId,
    @JsonProperty("products") List<ProductStockDTO> products
) implements Serializable {}