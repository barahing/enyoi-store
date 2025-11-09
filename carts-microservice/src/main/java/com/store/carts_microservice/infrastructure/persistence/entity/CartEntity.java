package com.store.carts_microservice.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("carts")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartEntity {
    @Id
    private UUID id;
    private UUID clientId;
    private BigDecimal total;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    @Column("order_id")
    private UUID orderId;
}
