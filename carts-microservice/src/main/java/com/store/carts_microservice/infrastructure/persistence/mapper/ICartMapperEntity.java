package com.store.carts_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartStatus;
import com.store.carts_microservice.infrastructure.persistence.entity.CartEntity;

@Mapper(componentModel = "spring")
public interface ICartMapperEntity {

    @Mapping(target = "cartId", source = "id")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToEnum")
    Cart toDomain(CartEntity entity);

    @Mapping(target = "id", source = "cartId")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToString")
    CartEntity toEntity(Cart domain);

    @Named("mapStatusToEnum")
    default CartStatus mapStatusToEnum(String status) {
        return status != null ? CartStatus.valueOf(status.toUpperCase()) : null;
    }

    @Named("mapStatusToString")
    default String mapStatusToString(CartStatus status) {
        return status != null ? status.name() : null;
    }
}
