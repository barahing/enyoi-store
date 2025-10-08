package com.store.products_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;

import com.store.products_microservice.domain.model.Product;
import com.store.products_microservice.infrastructure.persistence.entity.ProductEntity;

@Mapper(componentModel = "spring")
public interface ProductMapperEntity {
    Product toDomain (ProductEntity entity);
    ProductEntity toEntity (Product domain);
}
