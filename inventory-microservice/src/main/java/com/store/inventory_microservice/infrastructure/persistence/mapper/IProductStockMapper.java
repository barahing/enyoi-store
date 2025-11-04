package com.store.inventory_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping; 
import org.mapstruct.ReportingPolicy;

import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.infrastructure.persistence.entity.ProductStockEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IProductStockMapper {
    
    // Mapeo de Dominio a Entidad (Creation/Update)
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "currentStock", source = "currentStock")
    @Mapping(target = "reservedStock", source = "reservedStock")
    @Mapping(target = "updatedAt", source = "updatedAt") 
    // 💡 IGNORAR isNew: Esto resuelve el error de compilación de MapStruct
    ProductStockEntity toEntity(ProductStock domain);
    
    ProductStock toDomain(ProductStockEntity entity);
}