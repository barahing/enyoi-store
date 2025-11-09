package com.store.carts_microservice.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.model.CartStatus;
import com.store.carts_microservice.domain.ports.out.ICartRepositoryPort;
import com.store.carts_microservice.infrastructure.persistence.entity.CartEntity;
import com.store.carts_microservice.infrastructure.persistence.entity.CartItemEntity;
import com.store.carts_microservice.infrastructure.persistence.mapper.ICartEntityMapper;
import com.store.carts_microservice.infrastructure.persistence.repository.ICartR2dbcRepository;
import com.store.carts_microservice.infrastructure.persistence.repository.ICartItemR2dbcRepository;

import lombok.extern.slf4j.Slf4j;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CartRepositoryAdapter implements ICartRepositoryPort {
    
    private final ICartR2dbcRepository cartRepository;
    private final ICartItemR2dbcRepository cartItemRepository;
    private final ICartEntityMapper cartMapper;

    @Override
    public Mono<Cart> create(Cart cart) {
        CartEntity cartEntity = cartMapper.toEntity(cart);
        cartEntity.setId(null); // Forzar INSERT

        return cartRepository.save(cartEntity)
            .flatMap(savedCart -> {
                List<CartItemEntity> itemEntities = cart.getItems().stream()
                    .map(item -> toCartItemEntity(item, savedCart.getId()))
                    .collect(Collectors.toList());
                
                return cartItemRepository.saveAll(itemEntities)
                    .then(Mono.just(savedCart));
            })
            .flatMap(savedCart -> findByIdWithItems(savedCart.getId()));
    }

    @Override
    public Mono<Cart> update(Cart cart) {
        CartEntity cartEntity = cartMapper.toEntity(cart);

        return cartRepository.existsById(cartEntity.getId())
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new IllegalArgumentException("Cart not found with id: " + cartEntity.getId()));
                }
                return cartRepository.save(cartEntity);
            })
            .flatMap(savedCart -> {
                List<CartItemEntity> itemEntities = cart.getItems().stream()
                    .map(item -> toCartItemEntity(item, savedCart.getId()))
                    .collect(Collectors.toList());

                return cartItemRepository.deleteAllByCartId(savedCart.getId())
                    .thenMany(cartItemRepository.saveAll(itemEntities))
                    .then(Mono.just(savedCart));
            })
            .flatMap(savedCart -> findByIdWithItems(savedCart.getId()));
    }
    
    @Override
    public Mono<Cart> findById(UUID id) {
        return findByIdWithItems(id);
    }
    
    @Override
    public Mono<Cart> findActiveCartByClientId(UUID clientId) {
        return cartRepository.findByClientIdAndStatus(clientId, "ACTIVE")
            .flatMap(this::mapCartEntityToDomainWithItems);
    }

    @Override
    public Mono<Cart> findByOrderId(UUID orderId) {
        log.debug("🔍 Searching cart with orderId={}", orderId);

        return cartRepository.findByOrderId(orderId)
            .flatMap(this::mapCartEntityToDomainWithItems)
            .switchIfEmpty(Mono.defer(() -> {
                log.warn("⚠️ No cart found for orderId={}", orderId);
                return Mono.empty();
            }));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return cartItemRepository.deleteAllByCartId(id)
            .then(cartRepository.deleteById(id));
    }

    @Override
    public Mono<Void> deleteCartByOrderId(UUID orderId) {
        return findByOrderId(orderId)
            .flatMap((Cart cart) -> cartRepository.deleteById(cart.getCartId()))
            .then();
    }


    @Override
    public Mono<Cart> findByStatus(String status) {
        return cartRepository.findByStatus(status)
            .flatMap(this::mapCartEntityToDomainWithItems);
    }

    @Override
    public Mono<Void> updateCartStatusByOrderId(UUID orderId, CartStatus newStatus) {
        return findByOrderId(orderId)
            .flatMap(cart -> {
                cart.setStatus(newStatus);
                CartEntity entity = cartMapper.toEntity(cart);
                return cartRepository.save(entity).then();
            })
            .then();
    }

    @Override
    public Mono<Void> linkOrderToCart(UUID clientId, UUID orderId) {
        log.info("🟡 Linking order {} to cart of client {}", orderId, clientId);

        return cartRepository.findByClientIdAndStatus(clientId, "ACTIVE")
            .flatMap(cartEntity -> {
                cartEntity.setOrderId(orderId);
                cartEntity.setStatus(CartStatus.CONVERTING.name());
                return cartRepository.save(cartEntity).then();
            })
            .then()
            .doOnSuccess(v -> log.info("✅ Linked cart to orderId {}", orderId))
            .doOnError(e -> log.error("❌ Failed to link cart to orderId {}: {}", orderId, e.getMessage()));
    }

    // ✅ Métodos auxiliares

    private CartItemEntity toCartItemEntity(CartItem domain, UUID cartId) {
        CartItemEntity entity = new CartItemEntity();
        entity.setProductId(domain.productId());
        entity.setQuantity(domain.quantity());
        entity.setPrice(domain.price());
        entity.setSubtotal(domain.subtotal());
        entity.setCartId(cartId);
        return entity;
    }

    private CartItem toCartItemDomain(CartItemEntity entity) {
        return CartItem.create(entity.getProductId(), entity.getQuantity(), entity.getPrice());
    }

    private Mono<Cart> findByIdWithItems(UUID cartId) {
        return cartRepository.findById(cartId)
            .flatMap(this::mapCartEntityToDomainWithItems);
    }

    private Mono<Cart> mapCartEntityToDomainWithItems(CartEntity cartEntity) {
        return cartItemRepository.findByCartId(cartEntity.getId())
            .collectList()
            .map(itemEntities -> {
                List<CartItem> cartItems = itemEntities.stream()
                    .map(this::toCartItemDomain)
                    .collect(Collectors.toList());
                return cartMapper.toDomainWithItems(cartEntity, cartItems);
            });
    }
}
