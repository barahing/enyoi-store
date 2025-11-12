package com.store.carts_microservice.domain.ports.out;

import com.store.carts_microservice.infrastructure.persistence.entity.CartEntity;
import com.store.carts_microservice.infrastructure.persistence.entity.CartItemEntity;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ICartReportPersistencePort {
    Flux<CartEntity> findActiveCartsWithoutOrder();
    Flux<CartItemEntity> findItemsByCartId(UUID cartId);
}
