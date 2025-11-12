package com.store.products_microservice.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import com.store.products_microservice.domain.model.Category;
import com.store.products_microservice.infrastructure.web.dto.CategoryRequestDto;
import com.store.products_microservice.infrastructure.web.dto.CategoryResponseDto;

@Mapper(componentModel = "spring")
public interface CategoryMapperDto {
    CategoryResponseDto toResponseDto (Category category);
    Category toDomain (CategoryRequestDto requestDto);
}
