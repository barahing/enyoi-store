package com.store.carts_microservice.infrastructure.persistence.mapper;

import java.util.List;
import org.mapstruct.Mapper;

import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.infrastructure.persistence.entity.CartItemEntity;

@Mapper(componentModel = "spring")
public interface ICartItemMapperEntity {

    CartItem toDomain(CartItemEntity entity);
    CartItemEntity toEntity(CartItem domain);

    List<CartItem> toDomainList(List<CartItemEntity> entities);
    List<CartItemEntity> toEntityList(List<CartItem> domains);
}
