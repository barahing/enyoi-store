package com.store.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CartConvertedEvent(
    UUID orderId,
    UUID clientId,
    BigDecimal total,
    LocalDateTime conversionDate,
    List<CartItemData> items
) {
    public record CartItemData(
        UUID productId,
        Integer quantity,
        BigDecimal price,
        BigDecimal subtotal
    ) {}
}