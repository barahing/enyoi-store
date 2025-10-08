package com.store.products_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;

import com.store.products_microservice.domain.model.Category;
import com.store.products_microservice.infrastructure.persistence.entity.CategoryEntity;

@Mapper(componentModel = "spring")
public interface CategoryMapperEntity {
    Category toDomain (CategoryEntity entity);
    CategoryEntity toEntity (Category domain);
}
