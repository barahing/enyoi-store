package com.store.carts_microservice.domain.ports.out;

import com.store.carts_microservice.domain.model.Cart;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface IOrderServicePort {
    Mono<UUID> createOrderFromCart(Cart cart);
}