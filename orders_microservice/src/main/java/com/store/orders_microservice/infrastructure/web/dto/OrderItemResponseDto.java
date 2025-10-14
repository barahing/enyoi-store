package com.store.orders_microservice.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemResponseDto {
    private UUID productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
