package com.store.purchases_microservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class PurchaseOrder {

    private UUID id;
    private String supplierName;
    private UUID productId;
    private Integer quantity;
    private BigDecimal unitCost;
    private PurchaseStatus status; 
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    
    public static PurchaseOrder createNew(String supplierName, UUID productId, Integer quantity, BigDecimal unitCost) {
        return PurchaseOrder.builder()
            .id(UUID.randomUUID())
            .supplierName(supplierName)
            .productId(productId)
            .quantity(quantity)
            .unitCost(unitCost)
            .status(PurchaseStatus.PENDING)
            .orderDate(LocalDateTime.now())
            .build();
    }
    
    public PurchaseOrder markAsReceived() {
        if (this.status != PurchaseStatus.PENDING) {
            throw new IllegalStateException("Purchase Order is not in PENDING state.");
        }
        return this.toBuilder()
            .status(PurchaseStatus.RECEIVED)
            .deliveryDate(LocalDateTime.now())
            .build();
    }
}