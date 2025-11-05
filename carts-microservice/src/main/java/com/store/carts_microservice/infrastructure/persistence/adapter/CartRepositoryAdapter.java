package com.store.carts_microservice.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.ports.out.ICartRepositoryPort;
import com.store.carts_microservice.infrastructure.persistence.entity.CartEntity;
import com.store.carts_microservice.infrastructure.persistence.entity.CartItemEntity;
import com.store.carts_microservice.infrastructure.persistence.mapper.ICartEntityMapper;
import com.store.carts_microservice.infrastructure.persistence.mapper.ICartItemEntityMapper;
import com.store.carts_microservice.infrastructure.persistence.repository.ICartR2dbcRepository;
import com.store.carts_microservice.infrastructure.persistence.repository.ICartItemR2dbcRepository;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements ICartRepositoryPort {
    
    private final ICartR2dbcRepository cartRepository;
    private final ICartItemR2dbcRepository cartItemRepository;
    private final ICartEntityMapper cartMapper;
    private final ICartItemEntityMapper cartItemMapper;
    
    @Override
    public Mono<Cart> save(Cart cart) {
        CartEntity cartEntity = cartMapper.toEntity(cart);
        
        // Guardar el carrito primero
        return cartRepository.save(cartEntity)
            .flatMap(savedCart -> {
                // Convertir items del dominio a entidades
                List<CartItemEntity> itemEntities = cart.getItems().stream()
                    .map(item -> {
                        CartItemEntity itemEntity = cartItemMapper.toEntity(item);
                        itemEntity.setCartId(savedCart.getId());
                        return itemEntity;
                    })
                    .collect(Collectors.toList());
                
                // Eliminar items existentes y guardar los nuevos
                return cartItemRepository.deleteAllByCartId(savedCart.getId())
                    .thenMany(cartItemRepository.saveAll(itemEntities))
                    .then(Mono.just(savedCart));
            })
            .flatMap(savedCart -> this.findByIdWithItems(savedCart.getId()));
    }
    
    @Override
    public Mono<Cart> findById(UUID id) {
        return this.findByIdWithItems(id);
    }
    
    @Override
    public Mono<Cart> findActiveCartByClientId(UUID clientId) {
        return cartRepository.findByClientIdAndStatus(clientId, "ACTIVE")
            .flatMap(this::mapCartEntityToDomainWithItems);
    }
    
    @Override
    public Mono<Void> deleteById(UUID id) {
        return cartItemRepository.deleteAllByCartId(id)
            .then(cartRepository.deleteById(id));
    }
    
    // Método auxiliar para cargar cart con items
    private Mono<Cart> findByIdWithItems(UUID cartId) {
        return cartRepository.findById(cartId)
            .flatMap(this::mapCartEntityToDomainWithItems);
    }
    
    private Mono<Cart> mapCartEntityToDomainWithItems(CartEntity cartEntity) {
        return cartItemRepository.findByCartId(cartEntity.getId())
            .collectList()
            .map(itemEntities -> {
                List<CartItem> cartItems = itemEntities.stream()
                    .map(cartItemMapper::toDomain)
                    .collect(java.util.stream.Collectors.toList());
                
                return cartMapper.toDomainWithItems(cartEntity, cartItems);
            });
    }
}