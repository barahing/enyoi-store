package com.store.carts_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.carts_microservice.infrastructure.persistence.entity.CartItemEntity;

import reactor.core.publisher.Flux;

public interface ICartItemR2dbcRepository extends ReactiveCrudRepository <CartItemEntity, UUID> {
    Flux<CartItemEntity> findByCartId(UUID cartId);

}
