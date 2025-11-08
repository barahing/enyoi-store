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
import com.store.carts_microservice.infrastructure.persistence.repository.ICartR2dbcRepository;
import com.store.carts_microservice.infrastructure.persistence.repository.ICartItemR2dbcRepository;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements ICartRepositoryPort {
    
    private final ICartR2dbcRepository cartRepository;
    private final ICartItemR2dbcRepository cartItemRepository;
    private final ICartEntityMapper cartMapper;
    // Eliminamos ICartItemEntityMapper y usamos métodos manuales
    
    @Override
    public Mono<Cart> create(Cart cart) {
        CartEntity cartEntity = cartMapper.toEntity(cart);
        
        // ✅ FORZAR INSERT: Establecer ID como null para que Spring Data haga INSERT
        cartEntity.setId(null);
        
        return cartRepository.save(cartEntity)
            .flatMap(savedCart -> {
                List<CartItemEntity> itemEntities = cart.getItems().stream()
                    .map(item -> toCartItemEntity(item, savedCart.getId())) // ✅ Mapper manual
                    .collect(Collectors.toList());
                
                return cartItemRepository.saveAll(itemEntities)
                    .then(Mono.just(savedCart));
            })
            .flatMap(savedCart -> this.findByIdWithItems(savedCart.getId()));
    }

    @Override
    public Mono<Cart> update(Cart cart) {
        CartEntity cartEntity = cartMapper.toEntity(cart);
        
        // ✅ PARA UPDATE: Verificar que el cart existe primero
        return cartRepository.existsById(cartEntity.getId())
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new IllegalArgumentException("Cart not found with id: " + cartEntity.getId()));
                }
                return cartRepository.save(cartEntity);
            })
            .flatMap(savedCart -> {
                List<CartItemEntity> itemEntities = cart.getItems().stream()
                    .map(item -> toCartItemEntity(item, savedCart.getId())) // ✅ Mapper manual
                    .collect(Collectors.toList());
                
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
    
    // ✅ MÉTODOS MANUALES PARA CART ITEM MAPPING
    private CartItemEntity toCartItemEntity(CartItem domain, UUID cartId) {
        if (domain == null) {
            return null;
        }
        
        CartItemEntity entity = new CartItemEntity();
        entity.setProductId(domain.productId());
        entity.setQuantity(domain.quantity());
        entity.setPrice(domain.price());
        entity.setSubtotal(domain.subtotal());
        entity.setCartId(cartId);
        // El id se generará automáticamente
        return entity;
    }
    
    private CartItem toCartItemDomain(CartItemEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Usar el método create del record CartItem
        return CartItem.create(
            entity.getProductId(),
            entity.getQuantity(), 
            entity.getPrice()
        );
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
                    .map(this::toCartItemDomain) // ✅ Mapper manual
                    .collect(Collectors.toList());
                
                return cartMapper.toDomainWithItems(cartEntity, cartItems);
            });
    }
}