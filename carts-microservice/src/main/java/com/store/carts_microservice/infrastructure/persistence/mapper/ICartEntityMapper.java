package com.store.carts_microservice.infrastructure.persistence.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.model.CartStatus;
import com.store.carts_microservice.infrastructure.persistence.entity.CartEntity;
import com.store.carts_microservice.infrastructure.persistence.entity.CartItemEntity;

@Mapper(componentModel = "spring")
public abstract class ICartEntityMapper {
    
    @Autowired
    protected ICartItemEntityMapper itemMapper;

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToEnum")
    @Mapping(target = "items", ignore = true)
    public abstract Cart toDomain(CartEntity entity);

    @Mapping(target = "id", source = "cartId")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToString")
    public abstract CartEntity toEntity(Cart domain);

    @Named("mapStatusToEnum")
    protected CartStatus mapStatusToEnum(String status) {
        return status != null ? CartStatus.valueOf(status.toUpperCase()) : null;
    }

    @Named("mapStatusToString")
    protected String mapStatusToString(CartStatus status) {
        return status != null ? status.name() : null;
    }
    
    public List<CartItem> toDomainList(List<CartItemEntity> entities) {
        return entities.stream().map(itemMapper::toDomain).toList();
    }
    
    public List<CartItemEntity> toEntityList(List<CartItem> domains) {
        return domains.stream().map(itemMapper::toEntity).toList();
    }
}