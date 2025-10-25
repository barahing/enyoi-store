package com.store.products_microservice.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import com.store.products_microservice.domain.factory.ProductFactory;
import com.store.products_microservice.domain.model.Product;
import com.store.products_microservice.infrastructure.web.dto.ProductRequestDto;
import com.store.products_microservice.infrastructure.web.dto.ProductResponseDto;

@Mapper(componentModel = "spring")
public abstract class ProductMapperDto { 

    public abstract ProductResponseDto toResponseDto(Product product);

    public Product toDomain(ProductRequestDto requestDto) {
        return ProductFactory.createNew(
            requestDto.getName(),
            requestDto.getDescription(),
            requestDto.getPrice(),
            requestDto.getInitialStock(), 
            requestDto.getCategoryId()
        );
    }
}