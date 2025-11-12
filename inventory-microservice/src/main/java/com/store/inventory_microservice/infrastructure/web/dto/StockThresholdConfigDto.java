package com.store.inventory_microservice.infrastructure.web.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockThresholdConfigDto {
    private Integer thresholdValue;
    private LocalDateTime updatedAt;
}
