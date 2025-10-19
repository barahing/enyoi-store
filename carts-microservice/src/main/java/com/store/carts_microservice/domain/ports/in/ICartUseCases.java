package com.store.carts_microservice.domain.ports.in;

import java.util.UUID;

import com.store.carts_microservice.domain.model.Cart;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICartUseCases {
    Mono<Cart> createCart(Cart cart);
    Mono<Cart> getCartById(UUID id);
    Flux<Cart> getAllCarts();
    Mono<Cart> modifyCart(UUID id, Cart updatedCart);
    Mono<Cart> updateCart(UUID id, Cart updatedCart);
    Mono<Void> deleteCart(UUID id);
    Mono<Cart> confirmCartToOrder(UUID id);
}
