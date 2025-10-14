package com.store.orders_microservice.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import com.store.orders_microservice.domain.model.OrderItem;
import com.store.orders_microservice.infrastructure.web.dto.OrderItemRequestDto;
import com.store.orders_microservice.infrastructure.web.dto.OrderItemResponseDto;

@Mapper(componentModel = "spring")
public interface OrderItemMapperDto {

    default OrderItem toDomain(OrderItemRequestDto dto) {
        if (dto == null) return null;
        return OrderItem.create(dto.getProductId(), dto.getQuantity(), dto.getPrice());
    }

    OrderItemResponseDto toDto(OrderItem orderItem);
}
