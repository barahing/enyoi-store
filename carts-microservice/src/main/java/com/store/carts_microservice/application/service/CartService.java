package com.store.carts_microservice.application.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.store.carts_microservice.domain.factory.CartFactory;
import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;
import com.store.carts_microservice.domain.ports.out.ICartRepositoryPort;
import com.store.carts_microservice.domain.ports.out.ICartEventPublisherPort;
import com.store.carts_microservice.domain.ports.out.IInventoryServicePort;

import com.store.common.events.CartConvertedEvent;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CartService implements ICartServicePort {

    private final ICartRepositoryPort cartRepository;
    private final ICartEventPublisherPort eventPublisher;
    private final IInventoryServicePort inventoryService;
    
    @Override
    public Mono<Cart> createCartForClient(UUID clientId) {
        return cartRepository.findActiveCartByClientId(clientId)
                .switchIfEmpty(
                    Mono.fromCallable(() -> CartFactory.createNewCart(clientId))
                        .flatMap(cart -> cartRepository.create(cart))
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
            // SOLO validar stock disponible
            .flatMap(cart -> 
                inventoryService.isQuantityAvailable(newItem.productId(), newItem.quantity())
                    .filter(Boolean::booleanValue)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("Insufficient stock.")))
                    .then(Mono.just(cart))
            )
            .map(cart -> {
                cart.addItem(newItem);
                return cart;
            })
            .flatMap(cart -> cartRepository.update(cart));
    }
    
    @Override
    public Mono<Cart> updateItemQuantity(UUID cartId, UUID productId, int newQuantity) {
        return cartRepository.findById(cartId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
            .map(cart -> {
                cart.updateItemQuantity(productId, newQuantity);
                return cart;
            })
            .flatMap(cart -> cartRepository.update(cart));
    }

    @Override
    public Mono<Cart> removeProductFromCart(UUID cartId, UUID productId) {
        return cartRepository.findById(cartId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
            .map(cart -> {
                cart.removeItem(productId);
                return cart;
            })
            .flatMap(cart -> cartRepository.update(cart));
    }

    @Override
    public Mono<Void> deleteCart(UUID id) {
        return cartRepository.deleteById(id);
    }

    @Override
    public Mono<Cart> convertCartToOrder(UUID cartId) {
        return cartRepository.findById(cartId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
            .filter(Cart::isConvertible)
            .switchIfEmpty(Mono.error(new IllegalStateException("Cart is not convertible.")))
            
            // SOLO validar stock disponible
            .flatMap(cart -> {
                Mono<Cart> result = Mono.just(cart);
                for (CartItem item : cart.getItems()) {
                    result = result.flatMap(c -> 
                        inventoryService.isQuantityAvailable(item.productId(), item.quantity())
                            .filter(Boolean::booleanValue)
                            .switchIfEmpty(Mono.error(new IllegalStateException("Insufficient stock for product: " + item.productId())))
                            .thenReturn(c)
                    );
                }
                return result;
            })
            
            // PUBLICAR EVENTO CartConvertedEvent
            .flatMap(cart -> {
                BigDecimal total = cart.getItems().stream()
                    .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                List<CartConvertedEvent.CartItemData> itemDataList = cart.getItems().stream()
                    .map(item -> new CartConvertedEvent.CartItemData(
                        item.productId(),
                        item.quantity(),
                        item.price(),
                        item.price().multiply(BigDecimal.valueOf(item.quantity()))
                    ))
                    .collect(Collectors.toList());
                
                CartConvertedEvent event = new CartConvertedEvent(
                    UUID.randomUUID(),
                    cart.getClientId(),
                    total,
                    LocalDateTime.now(),
                    itemDataList,
                    null
                );
                
                return eventPublisher.publishCartConverted(event)
                    .then(Mono.fromCallable(() -> {
                        cart.markAsConverting();
                        return cart;
                    }))
                    .flatMap(updatedCart -> cartRepository.update(updatedCart))
                    .then(createCartForClient(cart.getClientId()));
            });
    }

    @Override
    public Mono<Cart> findById(UUID cartId) {
        return cartRepository.findById(cartId);
    }

    // Nuevo método para eliminar carrito de usuario (para User Service)
    public Mono<Void> deleteUserCart(UUID clientId) {
        return cartRepository.findActiveCartByClientId(clientId)
            .flatMap(cart -> cartRepository.deleteById(cart.getCartId()))
            .then();
    }

    
}