package com.store.inventory_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.infrastructure.persistence.entity.ProductStockEntity;

@Mapper (componentModel = "spring")
public interface IProductStockEntityMapper {
    
    @Mapping(target = "availableStock", ignore = true)
    ProductStock toDomain(ProductStockEntity entity);
    
    @Mapping(target = "updatedAt", ignore = true)
    ProductStockEntity toEntity(ProductStock domain);
}
