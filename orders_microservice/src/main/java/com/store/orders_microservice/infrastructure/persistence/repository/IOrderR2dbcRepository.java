package com.store.orders_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.orders_microservice.infrastructure.persistence.entity.OrderEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IOrderR2dbcRepository extends ReactiveCrudRepository<OrderEntity, UUID> {
    Mono<OrderEntity> findByClientId(UUID clientId);
    Flux<OrderEntity> findAllByStatus(String status);
}
