package com.store.common.commands;

import java.util.List;
import java.util.UUID;
import com.store.common.dto.ProductStockDTO;

public record ReserveStockCommand(
    UUID orderId,
    List<ProductStockDTO> items
) {
    
}