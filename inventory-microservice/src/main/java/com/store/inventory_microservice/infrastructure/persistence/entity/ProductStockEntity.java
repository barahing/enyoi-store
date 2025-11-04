package com.store.inventory_microservice.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Getter; // Reemplaza @Data
import lombok.Setter; // Reemplaza @Data
import lombok.NoArgsConstructor; // Necesario para R2DBC/Spring Data

@Getter 
@Setter
@NoArgsConstructor // Reintroduce el constructor vacío requerido por Spring Data
@Table("product_stock")
public class ProductStockEntity implements Persistable<UUID> {

    @Id
    private UUID productId;
    
    private int currentStock;
    private int reservedStock;
    private LocalDateTime updatedAt;

    // Campo Transient, solo para tracking del estado.
    @Transient 
    private boolean stateNew; // Inicialización removida aquí.

    // Método de Fábrica para CREACIÓN (Siempre fuerza INSERT)
    public static ProductStockEntity newEntity(UUID productId, int currentStock, int reservedStock) {
        ProductStockEntity entity = new ProductStockEntity();
        entity.productId = productId;
        entity.currentStock = currentStock;
        entity.reservedStock = reservedStock;
        entity.updatedAt = null;
        entity.setNew(true); // Usamos el setter explícito
        return entity;
    }

    // --- Métodos de Persistable ---

    @Override
    public UUID getId() {
        return productId;
    }

    // Devuelve el estado interno.
    @Override
    public boolean isNew() {
        return this.stateNew;
    }

    // Setter utilizado por el Adaptador/R2DBC para forzar el estado.
    public void setNew(boolean isNew) {
        this.stateNew = isNew;
    }
}