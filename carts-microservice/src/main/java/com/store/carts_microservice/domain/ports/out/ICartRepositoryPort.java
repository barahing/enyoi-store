package com.store.carts_microservice.domain.ports.out;

import java.util.UUID;
import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartStatus;

import reactor.core.publisher.Mono;

public interface ICartRepositoryPort {
    Mono<Cart> create(Cart cart);  
    Mono<Cart> update(Cart cart);  
    Mono<Cart> findById(UUID id);
    Mono<Cart> findActiveCartByClientId(UUID clientId);
    Mono<Cart> findByOrderId(UUID orderId);
    Mono<Void> deleteById(UUID id);
    Mono<Void> deleteCartByOrderId(UUID orderId);
    Mono<Cart> findByStatus(String status);
    Mono<Void> updateCartStatusByOrderId(UUID orderId, CartStatus newStatus);
    Mono<Void> linkOrderToCart(UUID clientId, UUID orderId);
    Mono<Cart> findByClientIdAndStatus(UUID clientId, CartStatus status);


}