package com.store.orders_microservice.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import com.store.orders_microservice.domain.model.OrderItem;
import com.store.orders_microservice.infrastructure.web.dto.OrderItemRequestDto;
import com.store.orders_microservice.infrastructure.web.dto.OrderItemResponseDto;

@Mapper(componentModel = "spring")
public interface OrderItemMapperDto {

    default OrderItem toDomain(OrderItemRequestDto dto) {
        if (dto == null) return null;
        // Calcula subtotal directamente usando el método de dominio
        return OrderItem.create(dto.getProductId(), dto.getQuantity(), dto.getPrice());
    }

    default OrderItemResponseDto toResponseDto(OrderItem orderItem) {
        if (orderItem == null) return null;
        return new OrderItemResponseDto(
            orderItem.productId(),
            orderItem.quantity(),
            orderItem.price(),
            orderItem.subtotal()
        );
    }
}
