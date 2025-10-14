package com.store.orders_microservice.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.infrastructure.web.dto.OrderRequestDto;
import com.store.orders_microservice.infrastructure.web.dto.OrderResponseDto;

@Mapper(componentModel = "spring", uses = { OrderItemMapperDto.class })
public interface OrderMapperDto {

    @Mappings({
        @Mapping(target = "orderId", ignore = true)
    })
    Order toDomain(OrderRequestDto requestDto);

    OrderResponseDto toResponseDto(Order order);

    //List<OrderItemResponseDto> toItemResponseDtos(List<OrderItem> items);
}
