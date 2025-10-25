package com.store.carts_microservice.domain.ports.in;

import java.util.UUID;
import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import reactor.core.publisher.Mono;

public interface ICartServicePort {
    
    Mono<Cart> createCartForClient(UUID clientId);
    Mono<Cart> getActiveCartByClientId(UUID clientId);
    Mono<Cart> addProductToCart(UUID cartId, CartItem item);
    Mono<Cart> updateItemQuantity(UUID cartId, UUID productId, int newQuantity);
    Mono<Cart> removeProductFromCart(UUID cartId, UUID productId);
    Mono<Void> deleteCart(UUID id);
    Mono<Cart> convertCartToOrder(UUID cartId);
}