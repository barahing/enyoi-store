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
import com.store.carts_microservice.domain.model.CartStatus;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;
import com.store.carts_microservice.domain.ports.out.ICartRepositoryPort;
import com.store.carts_microservice.domain.ports.out.ICartEventPublisherPort;
import com.store.carts_microservice.domain.ports.out.IUserServicePort; 
import com.store.carts_microservice.domain.ports.out.IInventoryServicePort;
import com.store.carts_microservice.domain.ports.out.IProductServicePort;

import com.store.common.events.CartConvertedEvent;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CartService implements ICartServicePort {

    private final ICartRepositoryPort cartRepository;
    private final ICartEventPublisherPort eventPublisher;
    private final IUserServicePort userService;
    private final IInventoryServicePort inventoryService;
    private final IProductServicePort productService;
    
    @Override
    public Mono<Cart> createCartForClient(UUID clientId) {
        return cartRepository.findActiveCartByClientId(clientId)
                .switchIfEmpty(
                    Mono.fromCallable(() -> CartFactory.createNewCart(clientId))
                        .flatMap(cart -> cartRepository.save(cart)) // ✅ CORREGIDO
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
            // Validar que el producto existe y tiene stock
            .flatMap(cart -> 
                productService.productExists(newItem.productId())
                    .filter(Boolean::booleanValue)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("Product does not exist.")))
                    .then(inventoryService.isQuantityAvailable(newItem.productId(), newItem.quantity()))
                    .filter(Boolean::booleanValue)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("Insufficient stock.")))
                    .then(Mono.just(cart))
            )
            .map(cart -> {
                cart.addItem(newItem);
                return cart;
            })
            .flatMap(cart -> cartRepository.save(cart)); // ✅ CORREGIDO
    }
    
    @Override
    public Mono<Cart> updateItemQuantity(UUID cartId, UUID productId, int newQuantity) {
        return cartRepository.findById(cartId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
            .map(cart -> {
                cart.updateItemQuantity(productId, newQuantity);
                return cart;
            })
            .flatMap(cart -> cartRepository.save(cart)); // ✅ CORREGIDO
    }

    @Override
    public Mono<Cart> removeProductFromCart(UUID cartId, UUID productId) {
        return cartRepository.findById(cartId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
            .map(cart -> {
                cart.removeItem(productId);
                return cart;
            })
            .flatMap(cart -> cartRepository.save(cart)); // ✅ CORREGIDO
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
            
            // 1. VALIDACIÓN USUARIO
            .flatMap(cart -> 
                userService.getClientRole(cart.getClientId())
                    .filter(role -> "USER".equalsIgnoreCase(role))
                    .switchIfEmpty(Mono.error(new IllegalStateException("Client is not authorized to create orders.")))
                    .thenReturn(cart)
            )
            
            // 2. VALIDACIÓN PRODUCTOS Y STOCK
            .flatMap(cart -> {
                Mono<Cart> result = Mono.just(cart);
                
                for (CartItem item : cart.getItems()) {
                    result = result.flatMap(c -> 
                        productService.productExists(item.productId())
                            .filter(Boolean::booleanValue)
                            .switchIfEmpty(Mono.error(new IllegalStateException("Product no longer exists: " + item.productId())))
                            .then(inventoryService.isQuantityAvailable(item.productId(), item.quantity()))
                            .filter(Boolean::booleanValue)
                            .switchIfEmpty(Mono.error(new IllegalStateException("Insufficient stock for product: " + item.productId())))
                            .thenReturn(c)
                    );
                }
                
                return result;
            })
            
            // 3. RESERVAR STOCK TEMPORAL
            .flatMap(cart -> {
                List<Mono<Void>> reservations = cart.getItems().stream()
                    .map(item -> inventoryService.reserveStock(item.productId(), item.quantity()))
                    .collect(Collectors.toList());
                
                return Mono.zip(reservations, objects -> cart);
            })
            
            // 4. PUBLICAR EVENTO
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
                    itemDataList
                );
                
                return eventPublisher.publishCartConverted(event)
                    .then(Mono.fromCallable(() -> {
                        cart.markAsConverting();
                        return cart;
                    }))
                    .flatMap(updatedCart -> cartRepository.save(updatedCart));
            })
            
            // 5. MANEJO DE ERRORES - LIBERAR STOCK SI FALLA
            .doOnError(error -> {
                // Liberar stock reservado si algo falla después de la reserva
                cartRepository.findById(cartId).subscribe(cart -> {
                    cart.getItems().forEach(item -> {
                        inventoryService.releaseStockReservation(item.productId(), item.quantity())
                            .onErrorResume(e -> Mono.empty())
                            .subscribe();
                    });
                });
            });
    }
}