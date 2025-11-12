package com.store.payments_microservice.infrastructure.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class OrderDTO {
    private UUID orderId;
    private UUID clientId;
    private BigDecimal total;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private List<OrderItemDTO> items;
}
