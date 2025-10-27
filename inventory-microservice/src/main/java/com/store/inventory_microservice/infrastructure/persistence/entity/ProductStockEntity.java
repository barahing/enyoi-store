package com.store.inventory_microservice.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("product_stock")
public class ProductStockEntity {

    @Id
    @Column("product_id")
    private UUID productId;
    
    @Column("current_stock")
    private int currentStock;
    
    @Column("reserved_stock")
    private int reservedStock;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}