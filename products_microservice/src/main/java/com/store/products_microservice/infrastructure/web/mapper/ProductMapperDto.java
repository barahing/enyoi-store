package com.store.products_microservice.infrastructure.web.mapper;

import org.mapstruct.Mapper;

import com.store.products_microservice.domain.model.Product;
import com.store.products_microservice.infrastructure.web.dto.ProductRequestDto;
import com.store.products_microservice.infrastructure.web.dto.ProductResponseDto;

@Mapper(componentModel = "spring")
public interface ProductMapperDto {
    ProductResponseDto toResponseDto(Product product);
    Product toDomain(ProductRequestDto requestDto);
}
