package com.store.payments_microservice.infrastructure.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.Data;

@Data
public class OrderItemDTO {
    private UUID productId;
    private int quantity;
    private BigDecimal price;
}
