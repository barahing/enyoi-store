package com.store.purchases_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.purchases_microservice.infrastructure.persistence.entity.PurchaseOrderEntity;

public interface IPurchaseOrderRepository extends ReactiveCrudRepository<PurchaseOrderEntity, UUID> {

}
