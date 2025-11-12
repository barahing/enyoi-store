package com.store.inventory_microservice.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter; 
import lombok.Setter; 
import lombok.NoArgsConstructor; 

@Getter 
@Setter
@NoArgsConstructor 
@Table("product_stock")
public class ProductStockEntity implements Persistable<UUID> {

    @Id
    private UUID productId;
    
    private int currentStock;
    private int reservedStock;
    private LocalDateTime updatedAt;

    @Transient 
    private boolean stateNew; 

    public static ProductStockEntity newEntity(UUID productId, int currentStock, int reservedStock) {
        ProductStockEntity entity = new ProductStockEntity();
        entity.productId = productId;
        entity.currentStock = currentStock;
        entity.reservedStock = reservedStock;
        entity.updatedAt = null;
        entity.setNew(true); 
        return entity;
    }

    @Override
    public UUID getId() {
        return productId;
    }

    @Override
    public boolean isNew() {
        return this.stateNew;
    }

    public void setNew(boolean isNew) {
        this.stateNew = isNew;
    }
}