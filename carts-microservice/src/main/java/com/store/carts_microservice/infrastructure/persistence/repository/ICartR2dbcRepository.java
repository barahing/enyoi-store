package com.store.carts_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.carts_microservice.infrastructure.persistence.entity.CartEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICartR2dbcRepository extends ReactiveCrudRepository<CartEntity, UUID> {
    Flux<CartEntity> findByClientId(UUID clientId);
    Mono<CartEntity> findByClientIdAndStatus(UUID clientId, String status);
    
    @Query("SELECT * FROM carts WHERE order_id = :orderId")
    Mono<CartEntity> findByOrderId(UUID orderId);
    Mono<CartEntity> findByStatus(String status);

}
