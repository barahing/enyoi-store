package com.store.carts_microservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.store.carts_microservice.domain.factory.CartFactory;
import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;
import com.store.carts_microservice.domain.ports.out.ICartRepositoryPort;
import com.store.carts_microservice.domain.ports.out.ICartEventPublisherPort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CartService implements ICartServicePort {

    private final ICartRepositoryPort cartRepository;
    private final ICartEventPublisherPort eventPublisher;

    @Override
    public Mono<Cart> createCartForClient(UUID clientId) {
        return cartRepository.findActiveCartByClientId(clientId)
                .switchIfEmpty(
                    Mono.fromCallable(() -> CartFactory.createNewCart(clientId))
                        .flatMap(cartRepository::save) 
                );
    }

    @Override
    public Mono<Cart> getActiveCartByClientId(UUID clientId) {
        return cartRepository.findActiveCartByClientId(clientId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Active cart not found for client.")));
    }
    
    @Override
    public Mono<Cart> addProductToCart(UUID cartId, CartItem newItem) {
        return cartRepository.findById(cartId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
                .map(cart -> {
                    cart.addItem(newItem);
                    return cart;
                })
                .flatMap(cartRepository::save);
    }
    
    @Override
    public Mono<Cart> updateItemQuantity(UUID cartId, UUID productId, int newQuantity) {
        return cartRepository.findById(cartId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
                .map(cart -> {
                    cart.updateItemQuantity(productId, newQuantity);
                    return cart;
                })
                .flatMap(cartRepository::save);
    }

    @Override
    public Mono<Cart> removeProductFromCart(UUID cartId, UUID productId) {
        return cartRepository.findById(cartId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
                .map(cart -> {
                    cart.removeItem(productId);
                    return cart;
                })
                .flatMap(cartRepository::save);
    }

    @Override
    public Mono<Void> deleteCart(UUID id) {
        return cartRepository.deleteById(id);
    }

    @Override
    public Mono<Cart> convertCartToOrder(UUID cartId) {
        return cartRepository.findById(cartId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
                .flatMap(cart -> {
                    if (!cart.isConvertible()) {
                        return Mono.error(new IllegalStateException("Cart is not convertible (Status: " + cart.getStatus() + ")."));
                    }
                    cart.markAsConverted();
                    return cartRepository.save(cart)
                            .doOnNext(savedCart -> {
                                // 3. Publicar el evento para que orders-service cree la orden
                                // Aquí se construiría CartConvertedEvent a partir de savedCart
                                // eventPublisher.publishCartConverted(eventDto).subscribe();
                            })
                            .thenReturn(cart);
                });
    }
}