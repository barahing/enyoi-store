package com.store.carts_microservice.domain.ports.out;

import java.util.UUID;
import com.store.carts_microservice.domain.model.Cart;
import reactor.core.publisher.Mono;

public interface ICartRepositoryPort {
    Mono<Cart> create(Cart cart);  
    Mono<Cart> update(Cart cart);  
    Mono<Cart> findById(UUID id);
    Mono<Cart> findActiveCartByClientId(UUID clientId);
    Mono<Void> deleteById(UUID id);
}