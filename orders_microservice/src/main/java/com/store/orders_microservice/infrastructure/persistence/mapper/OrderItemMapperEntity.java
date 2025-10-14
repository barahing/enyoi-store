package com.store.orders_microservice.infrastructure.persistence.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import com.store.orders_microservice.domain.model.OrderItem;
import com.store.orders_microservice.infrastructure.entity.OrderItemEntity;

@Mapper(componentModel = "spring")
public interface OrderItemMapperEntity {
    OrderItem toDomain (OrderItemEntity entity);
    OrderItemEntity toEntity (OrderItem domain);

    List<OrderItem> toDomainList(List<OrderItemEntity> entities);
    List<OrderItemEntity> toEntityList(List<OrderItem> domains);
}
