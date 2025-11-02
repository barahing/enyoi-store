package com.store.purchases_microservice.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("purchase_order")
public class PurchaseOrderEntity {

    @Id
    private UUID id;
    private String supplierName;
    private UUID productId;
    private Integer quantity;
    private BigDecimal unitCost;
    private String status;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
}