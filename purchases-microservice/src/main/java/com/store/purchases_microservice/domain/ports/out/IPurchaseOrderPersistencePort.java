package com.store.purchases_microservice.domain.ports.out;

import com.store.purchases_microservice.domain.model.PurchaseOrder;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface IPurchaseOrderPersistencePort {
    Mono<PurchaseOrder> create(PurchaseOrder purchaseOrder);
    Mono<PurchaseOrder> update(PurchaseOrder purchaseOrder);
    Mono<PurchaseOrder> findById(UUID id);
}
