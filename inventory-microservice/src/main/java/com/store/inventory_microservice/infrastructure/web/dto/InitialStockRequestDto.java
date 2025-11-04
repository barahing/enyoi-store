package com.store.inventory_microservice.infrastructure.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitialStockRequestDto {
    
    @NotNull(message = "Product ID is required")
    private UUID productId;
    
    @NotNull(message = "Initial stock is required")
    @PositiveOrZero(message = "Initial stock cannot be negative")
    private Integer initialStock;
}