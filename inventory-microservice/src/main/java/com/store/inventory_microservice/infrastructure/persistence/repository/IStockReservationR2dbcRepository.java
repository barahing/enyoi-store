package com.store.inventory_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.inventory_microservice.infrastructure.persistence.entity.StockReservationEntity;

import reactor.core.publisher.Flux;

public interface IStockReservationR2dbcRepository extends ReactiveCrudRepository<StockReservationEntity, UUID> {
    
    Flux<StockReservationEntity> findByOrderId(UUID orderId);
}
