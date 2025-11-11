package com.store.purchases_microservice.domain.ports.in;

import com.store.purchases_microservice.domain.model.PurchaseOrder;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface IPurchaseServicePorts {
    
    Mono<PurchaseOrder> createPurchaseOrder(String supplierName, UUID productId, Integer quantity, BigDecimal unitCost);
    Mono<PurchaseOrder> receivePurchaseOrder(UUID purchaseOrderId);
}