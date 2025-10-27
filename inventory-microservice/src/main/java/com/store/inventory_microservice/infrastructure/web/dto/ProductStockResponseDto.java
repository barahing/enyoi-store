package com.store.inventory_microservice.infrastructure.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductStockResponseDto {
    private UUID productId;
    private int currentStock;
    private int reservedStock;
    private int availableStock; 
    private LocalDateTime updatedAt;

}