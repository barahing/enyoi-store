package com.store.carts_microservice.domain.ports.in;

import java.util.UUID;
import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.model.CartStatus;

import reactor.core.publisher.Mono;

public interface ICartServicePort {
    
    Mono<Cart> createCartForClient(UUID clientId);
    Mono<Cart> getActiveCartByClientId(UUID clientId);
    Mono<Cart> addProductToCart(UUID cartId, CartItem item);
    Mono<Cart> updateItemQuantity(UUID cartId, UUID productId, int newQuantity);
    Mono<Cart> removeProductFromCart(UUID cartId, UUID productId);
    Mono<Void> deleteCart(UUID id);
    Mono<Cart> convertCartToOrder(UUID cartId);
    Mono<Cart> findById(UUID cartId);
    Mono<Void> deleteUserCart(UUID clientId);
    Mono<Cart> findByOrderId(UUID orderId);
    Mono<Void> updateCartStatus(UUID clientId, CartStatus newStatus);
    Mono<Void> deleteCartByOrderId(UUID orderId);
    Mono<Cart> findCartByStatus(CartStatus status);
    Mono<Void> updateCartStatusByOrderId(UUID orderId, CartStatus newStatus);
    Mono<Void> linkOrderToCart(UUID clientId, UUID orderId);
    Mono<Void> recreateCartAfterStockReserved(UUID orderId);

}