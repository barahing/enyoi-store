package com.store.purchases_microservice.infrastructure.persistence.adapter;

import java.util.UUID;
import org.springframework.stereotype.Component;
import com.store.purchases_microservice.domain.model.PurchaseOrder;
import com.store.purchases_microservice.domain.ports.out.IPurchaseOrderPersistencePort;
import com.store.purchases_microservice.infrastructure.persistence.mapper.IPurchaseOrderEntityMapper;
import com.store.purchases_microservice.infrastructure.persistence.repository.IPurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PurchaseOrderPersistenceAdapter implements IPurchaseOrderPersistencePort {

    private final IPurchaseOrderRepository repository;
    private final IPurchaseOrderEntityMapper mapper;

    @Override
    public Mono<PurchaseOrder> save(PurchaseOrder purchaseOrder) {
        return Mono.just(purchaseOrder)
            .map(mapper::toEntity)
            .flatMap(repository::save)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<PurchaseOrder> findById(UUID id) {
        return repository.findById(id)
                   .map(mapper::toDomain);
    }
}