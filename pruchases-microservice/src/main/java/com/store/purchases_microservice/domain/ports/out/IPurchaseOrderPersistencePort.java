package com.store.purchases_microservice.domain.ports.out;

import com.store.purchases_microservice.domain.model.PurchaseOrder;
import reactor.core.publisher.Mono;

public interface IPurchaseOrderPersistencePort {
    
    Mono<PurchaseOrder> save(PurchaseOrder purchaseOrder);
    Mono<PurchaseOrder> findById(java.util.UUID id);
}