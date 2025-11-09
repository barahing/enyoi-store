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
import com.store.carts_microservice.domain.ports.out.IInventoryServicePort;

import com.store.common.events.CartConvertedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService implements ICartServicePort {

    private final ICartRepositoryPort cartRepository;
    private final ICartEventPublisherPort eventPublisher;
    private final IInventoryServicePort inventoryService;
    
    @Override
    public Mono<Cart> createCartForClient(UUID clientId) {
        return cartRepository.findActiveCartByClientId(clientId)
                .switchIfEmpty(
                    Mono.fromCallable(() -> CartFactory.createNewCart(clientId))
                        .flatMap(cartRepository::create)
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
            .flatMap(cart -> 
                inventoryService.isQuantityAvailable(newItem.productId(), newItem.quantity())
                    .filter(Boolean::booleanValue)
                    .switchIfEmpty(Mono.error(new IllegalArgumentException("Insufficient stock.")))
                    .thenReturn(cart)
            )
            .map(cart -> {
                cart.addItem(newItem);
                return cart;
            })
            .flatMap(cartRepository::update);
    }
    
    @Override
    public Mono<Cart> updateItemQuantity(UUID cartId, UUID productId, int newQuantity) {
        return cartRepository.findById(cartId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
            .map(cart -> {
                cart.updateItemQuantity(productId, newQuantity);
                return cart;
            })
            .flatMap(cartRepository::update);
    }

    @Override
    public Mono<Cart> removeProductFromCart(UUID cartId, UUID productId) {
        return cartRepository.findById(cartId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
            .map(cart -> {
                cart.removeItem(productId);
                return cart;
            })
            .flatMap(cartRepository::update);
    }

    @Override
    public Mono<Void> deleteCart(UUID id) {
        return cartRepository.deleteById(id);
    }

    @Override
public Mono<Cart> convertCartToOrder(UUID cartId) {
    log.info("🔵 [1] Starting convertCartToOrder for cartId: {}", cartId);
    
    return cartRepository.findById(cartId)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Cart not found.")))
        .filter(Cart::isConvertible)
        .switchIfEmpty(Mono.error(new IllegalStateException("Cart is not convertible.")))
        .doOnNext(cart -> log.info("🔵 [2] Cart found and convertible. Validating stock..."))

        // Validar stock para cada producto
        .flatMap(cart -> {
            Mono<Cart> validation = Mono.just(cart);
            for (CartItem item : cart.getItems()) {
                validation = validation.flatMap(c -> 
                    inventoryService.isQuantityAvailable(item.productId(), item.quantity())
                        .doOnNext(available -> log.info("🔵 [Stock Check] Product: {}, Available: {}", item.productId(), available))
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new IllegalStateException(
                            "Insufficient stock for product: " + item.productId())))
                        .thenReturn(c)
                );
            }
            return validation;
        })

        // Cambiar estado del carrito a CONVERTING
        .map(cart -> {
            cart.markAsConverting();
            return cart;
        })
        .flatMap(cartRepository::update)

        // Publicar evento CartConvertedEvent
        .flatMap(cart -> {
            log.info("🔵 [3] Preparing CartConvertedEvent for client: {}", cart.getClientId());
            
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
                cart.getClientId(),
                total,
                LocalDateTime.now(),
                itemDataList,
                null
            );

            log.info("🔵 [4] Publishing CartConvertedEvent for clientId: {}", cart.getClientId());
            return eventPublisher.publishCartConverted(event)
                .thenReturn(cart);
        })

        // Logs finales
        .doOnSuccess(cart -> log.info("✅ [CARTS] Cart conversion completed for client: {}", cart.getClientId()))
        .doOnError(error -> log.error("❌ [ERROR] in convertCartToOrder: {}", error.getMessage()));
}


    @Override
    public Mono<Cart> findById(UUID cartId) {
        return cartRepository.findById(cartId);
    }

    @Override
    public Mono<Void> deleteUserCart(UUID clientId) {
        return cartRepository.findActiveCartByClientId(clientId)
            .flatMap(cart -> cartRepository.deleteById(cart.getCartId()))
            .then();
    }

    @Override
    public Mono<Void> updateCartStatus(UUID clientId, CartStatus newStatus) {
        log.info("🔄 Updating cart status for client: {} to {}", clientId, newStatus);

        return cartRepository.findActiveCartByClientId(clientId)
            .switchIfEmpty(Mono.error(new IllegalStateException("No active cart found for client: " + clientId)))
            .flatMap(cart -> {
                cart.setStatus(newStatus);
                return cartRepository.update(cart);
            })
            .then()
            .doOnSuccess(v -> log.info("✅ Cart status updated to {} for client {}", newStatus, clientId))
            .doOnError(e -> log.error("❌ Error updating cart for client {}: {}", clientId, e.getMessage()));
    }
    
    @Override
    public Mono<Void> deleteCartByOrderId(UUID orderId) {
        log.info("🧹 Deleting cart with orderId: {}", orderId);
        return cartRepository.findByOrderId(orderId)
            .flatMap(cart -> cartRepository.deleteById(cart.getCartId()).then()) // 👈 aclaramos que devuelve Mono<Void>
            .then()
            .doOnSuccess(v -> log.info("✅ Cart deleted for orderId: {}", orderId))
            .doOnError(e -> log.error("❌ Error deleting cart for orderId {}: {}", orderId, e.getMessage()));
    }

    
    @Override
    public Mono<Cart> findCartByStatus(CartStatus status) {
        return cartRepository.findByStatus(status.name());
    }

    @Override
    public Mono<Void> updateCartStatusByOrderId(UUID orderId, CartStatus newStatus) {
        log.info("🛒 Updating cart status to {} for orderId={}", newStatus, orderId);
        return cartRepository.updateCartStatusByOrderId(orderId, newStatus)
            .doOnSuccess(v -> log.info("✅ Cart updated for orderId={}", orderId))
            .doOnError(e -> log.error("❌ Failed to update cart for orderId {}: {}", orderId, e.getMessage()));
    }

    @Override
    public Mono<Void> linkOrderToCart(UUID clientId, UUID orderId) {
        log.info("🟡 Linking order {} to cart of client {}", orderId, clientId);

        return cartRepository.findActiveCartByClientId(clientId)
            .switchIfEmpty(Mono.error(new IllegalStateException("No active cart found for client: " + clientId)))
            .flatMap(cart -> {
                cart.setOrderId(orderId);
                cart.setStatus(CartStatus.CONVERTING);
                return cartRepository.update(cart).then();
            })
            .doOnSuccess(v -> log.info("✅ Linked cart to orderId {}", orderId))
            .doOnError(e -> log.error("❌ Failed to link cart to orderId {}: {}", orderId, e.getMessage()));
    }
}
