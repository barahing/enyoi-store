package com.store.inventory_microservice.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.infrastructure.web.dto.ProductStockResponseDto;

@Mapper(componentModel = "spring")
public interface IProductStockMapperDto {
    
    @Mapping(target = "availableStock", source = "availableStock") 
    ProductStockResponseDto toDto(ProductStock domain);
   
}