package com.store.orders_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.store.orders_microservice.domain.model.OrderItem;
import com.store.orders_microservice.infrastructure.persistence.entity.OrderItemEntity;

@Mapper(componentModel = "spring")
public interface IOrderItemEntityMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderId", ignore = true) 
    OrderItemEntity toEntity(OrderItem domain);

    @Mapping(target = "withUpdatedQuantity", ignore = true)
    @Mapping(target = "withUpdatedPrice", ignore = true)
    OrderItem toDomain(OrderItemEntity entity);
}