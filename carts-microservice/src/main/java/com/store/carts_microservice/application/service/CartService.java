package com.store.carts_microservice.application.service;

import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.store.carts_microservice.domain.factory.CartFactory;
import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;
import com.store.carts_microservice.domain.ports.out.ICartRepositoryPort;
import com.store.carts_microservice.domain.ports.out.ICartEventPublisherPort;
import com.store.common.events.CartConvertedEvent;
import com.store.common.events.CartConvertedEvent.CartItemData;

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
                    // Lógica de negocio para añadir o actualizar el item
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
                    
                    // 1. Marcar el carrito como convertido y persistir el cambio
                    cart.markAsConverted();
                    return cartRepository.save(cart)
                            .flatMap(savedCart -> {
                                // 2. Construir el evento DTO
                                CartConvertedEvent eventDto = new CartConvertedEvent(
                                    savedCart.getCartId(),
                                    savedCart.getClientId(),
                                    savedCart.getTotal(),
                                    savedCart.getUpdatedDate(),
                                    savedCart.getItems().stream()
                                            .map(item -> new CartItemData(
                                                item.productId(),
                                                item.quantity(),
                                                item.price(),
                                                item.subtotal()
                                            ))
                                            .collect(Collectors.toList())
                                );
                                
                                // 3. Publicar el evento de manera asíncrona
                                return eventPublisher.publishCartConverted(eventDto).thenReturn(savedCart);
                            });
                });
    }
}