package com.store.orders_microservice.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {
    private UUID orderId;
    private UUID clientId;
    private BigDecimal total;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<OrderItemResponseDto> items;
}
