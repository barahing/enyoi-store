package com.store.carts_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.model.CartStatus;
import com.store.carts_microservice.infrastructure.persistence.entity.CartEntity;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ICartItemEntityMapper.class})
public abstract class ICartEntityMapper {
    
    @Autowired
    protected ICartItemEntityMapper cartItemMapper;

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToEnum")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "orderId", source = "orderId") // 👈 nuevo
    public abstract Cart toDomain(CartEntity entity);

    @Mapping(target = "id", source = "cartId")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToString")
    @Mapping(target = "orderId", source = "orderId") // 👈 nuevo
    public abstract CartEntity toEntity(Cart domain);

    @Named("mapStatusToEnum")
    protected CartStatus mapStatusToEnum(String status) {
        if (status == null) return null;
        try {
            return CartStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CartStatus.ACTIVE;
        }
    }

    @Named("mapStatusToString")
    protected String mapStatusToString(CartStatus status) {
        return status != null ? status.name() : null;
    }
    
    // ✅ Método manual para mapear con items
    public Cart toDomainWithItems(CartEntity entity, List<CartItem> items) {
        return new Cart(
            entity.getId(),
            entity.getClientId(),
            items,
            entity.getTotal(),
            mapStatusToEnum(entity.getStatus()),
            entity.getCreatedDate(),
            entity.getUpdatedDate(),
            entity.getOrderId() // 👈 nuevo parámetro
        );
    }
}
