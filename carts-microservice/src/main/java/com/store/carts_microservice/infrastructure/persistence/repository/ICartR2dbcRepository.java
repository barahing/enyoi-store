package com.store.carts_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.carts_microservice.infrastructure.persistence.entity.CartEntity;

import reactor.core.publisher.Flux;

public interface ICartR2dbcRepository extends ReactiveCrudRepository<CartEntity, UUID> {
    Flux<CartEntity> findByClientId(UUID clientId);

}
