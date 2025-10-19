package com.store.orders_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.model.OrderStatus;
import com.store.orders_microservice.infrastructure.persistence.entity.OrderEntity;


@Mapper (componentModel = "spring")
public interface IOrderMapperEntity {

    @Mapping(target = "orderId", source = "id") 
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToEnum")
    Order toDomain (OrderEntity entity);

    @Mapping(target = "id", source = "orderId") 
    @Mapping(target = "status", source = "status", qualifiedByName = "mapStatusToString")
    OrderEntity toEntity (Order domain);

    @Named("mapStatusToEnum")
    default OrderStatus mapStatusToEnum(String status) {
        return status != null ? OrderStatus.valueOf(status.toUpperCase()) : null;
    }

    @Named("mapStatusToString")
    default String mapStatusToString(OrderStatus status) {
        return status != null ? status.name() : null;
    }

}
