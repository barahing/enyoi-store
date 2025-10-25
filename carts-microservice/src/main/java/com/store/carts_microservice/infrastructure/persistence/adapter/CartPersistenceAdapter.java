package com.store.carts_microservice.infrastructure.persistence.adapter;

import java.util.UUID;
import org.springframework.stereotype.Component;

import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.ports.out.ICartRepositoryPort;
import com.store.carts_microservice.infrastructure.persistence.entity.CartEntity;
import com.store.carts_microservice.infrastructure.persistence.mapper.ICartEntityMapper;
import com.store.carts_microservice.infrastructure.persistence.mapper.ICartItemEntityMapper;
import com.store.carts_microservice.infrastructure.persistence.repository.ICartItemR2dbcRepository;
import com.store.carts_microservice.infrastructure.persistence.repository.ICartR2dbcRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CartPersistenceAdapter implements ICartRepositoryPort {
    
    private final ICartR2dbcRepository cartRepository;
    private final ICartItemR2dbcRepository itemRepository;
    private final ICartEntityMapper cartMapper;
    private final ICartItemEntityMapper itemMapper;

    @Override
    public Mono<Cart> save(Cart cart) {
        
        Mono<CartEntity> saveCartEntity = Mono.just(cart)
                .map(cartMapper::toEntity)
                .flatMap(cartRepository::save);
        
        return saveCartEntity.flatMap(savedCartEntity -> {
            
            UUID cartId = savedCartEntity.getId();
            
            Mono<Void> deleteOldItems = itemRepository.deleteAllByCartId(cartId);
            
            Mono<List<?>> saveNewItems = Flux.fromIterable(cart.getItems())
                    .map(itemMapper::toEntity)
                    .doOnNext(itemEntity -> itemEntity.setCartId(cartId))
                    .collectList()
                    .flatMapMany(itemRepository::saveAll)
                    .collectList();
            
            return Mono.when(deleteOldItems, saveNewItems)
                    .thenReturn(savedCartEntity)
                    .flatMap(this::loadCartWithItems);
        });
    }

    @Override
    public Mono<Cart> findById(UUID id) {
        return cartRepository.findById(id)
                .flatMap(this::loadCartWithItems);
    }

    @Override
    public Mono<Cart> findActiveCartByClientId(UUID clientId) {
        return cartRepository.findByClientIdAndStatus(clientId, com.store.carts_microservice.domain.model.CartStatus.ACTIVE.name())
                .flatMap(this::loadCartWithItems);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        Mono<Void> deleteItems = itemRepository.deleteAllByCartId(id);
        Mono<Void> deleteCart = cartRepository.deleteById(id);
        
        return Mono.when(deleteItems, deleteCart).then();
    }
    
    private Mono<Cart> loadCartWithItems(CartEntity entity) {
        return itemRepository.findAllByCartId(entity.getId())
                .collectList()
                .map(items -> {
                    Cart cart = cartMapper.toDomain(entity);
                    cart.setItems(cartMapper.toDomainList(items));
                    return cart;
                });
    }
}