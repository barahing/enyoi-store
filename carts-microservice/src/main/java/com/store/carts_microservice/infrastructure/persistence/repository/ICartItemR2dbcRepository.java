package com.store.carts_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.carts_microservice.infrastructure.persistence.entity.CartItemEntity;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICartItemR2dbcRepository extends ReactiveCrudRepository <CartItemEntity, UUID> {
    @Modifying
    @Query("DELETE FROM cart_items WHERE cart_id = :cartId")
    Mono<Void> deleteAllByCartId(UUID cartId);

    Flux<CartItemEntity> findByCartId(UUID cartId);
    Flux<CartItemEntity> findAllByCartId(UUID cartId);
}
