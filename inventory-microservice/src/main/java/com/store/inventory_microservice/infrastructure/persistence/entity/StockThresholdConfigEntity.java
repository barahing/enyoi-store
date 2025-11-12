package com.store.inventory_microservice.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("stock_threshold_config")
public class StockThresholdConfigEntity {
    @Id
    private UUID id;
    private Integer thresholdValue;
    private LocalDateTime updatedAt;
}
