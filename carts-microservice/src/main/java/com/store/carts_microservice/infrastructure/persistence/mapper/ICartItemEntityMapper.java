package com.store.carts_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.infrastructure.persistence.entity.CartItemEntity;

@Mapper(componentModel = "spring")
public interface ICartItemEntityMapper {

    @Mapping(target = "cartId", ignore = true)
    @Mapping(target = "id", ignore = true)
    CartItemEntity toEntity(CartItem domain);

    @Mapping(target = "withUpdatedQuantity", ignore = true)
    @Mapping(target = "withUpdatedPrice", ignore = true)
    CartItem toDomain(CartItemEntity entity);
}