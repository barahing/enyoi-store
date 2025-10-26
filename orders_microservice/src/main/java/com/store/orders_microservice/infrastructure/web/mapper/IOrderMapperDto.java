package com.store.orders_microservice.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.infrastructure.web.dto.OrderRequestDto;
import com.store.orders_microservice.infrastructure.web.dto.OrderResponseDto;

@Mapper(componentModel = "spring", uses = { IOrderItemMapperDto.class }) 
public interface IOrderMapperDto {

    @Mappings({
        @Mapping(target = "orderId", ignore = true),
        @Mapping(target = "total", ignore = true), 
        @Mapping(target = "status", ignore = true),
        @Mapping(target = "createdDate", ignore = true),
        @Mapping(target = "updatedDate", ignore = true),
        @Mapping(target = "items", source = "items") 
    })
    Order toDomain(OrderRequestDto requestDto);

    OrderResponseDto toResponseDto(Order order);
}