package com.store.inventory_microservice.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("stock_reservations")
public class StockReservationEntity {

    @Id
    private UUID id;

    @Column("order_id")
    private UUID orderId;

    @Column("product_id")
    private UUID productId;

    private Integer quantity;
    
    @Column("reserved_at")
    private LocalDateTime reservedAt;
}
