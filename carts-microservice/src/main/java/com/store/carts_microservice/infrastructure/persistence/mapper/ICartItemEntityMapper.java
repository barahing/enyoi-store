package com.store.carts_microservice.infrastructure.persistence.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.infrastructure.persistence.entity.CartItemEntity;

@Component
public class ICartItemEntityMapper {

    public CartItemEntity toEntity(CartItem domain, UUID cartId) {
        if (domain == null) {
            return null;
        }
        
        CartItemEntity entity = new CartItemEntity();
        entity.setProductId(domain.productId());
        entity.setQuantity(domain.quantity());
        entity.setPrice(domain.price());
        entity.setSubtotal(domain.subtotal());
        entity.setCartId(cartId);
        return entity;
    }

    public CartItem toDomain(CartItemEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return CartItem.create(
            entity.getProductId(),
            entity.getQuantity(), 
            entity.getPrice()
        );
    }
}