package com.store.products_microservice.infrastructure.web.controller;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.store.products_microservice.domain.ports.in.ICategoryUseCases;
import com.store.products_microservice.infrastructure.web.dto.CategoryRequestDto;
import com.store.products_microservice.infrastructure.web.dto.CategoryResponseDto;
import com.store.products_microservice.infrastructure.web.mapper.CategoryMapperDto;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final ICategoryUseCases categoryUseCases;
    private final CategoryMapperDto categoryMapper;

    @GetMapping()
    public Flux<CategoryResponseDto> findAll() {
        return categoryUseCases.getAllCategory()
                .map(categoryMapper::toResponseDto);
    }

    @GetMapping("/{categoryId}")
    public Mono<CategoryResponseDto> findById(@Valid @PathVariable("categoryId") UUID categoryId) {
        return categoryUseCases.getCategoryById(categoryId)
                .map(categoryMapper::toResponseDto);
    }

    @PutMapping("/{categoryId}")
    public Mono<CategoryResponseDto> updateCategory(@Valid @PathVariable("categoryId") UUID categoryId, @RequestBody CategoryRequestDto category) {
        return categoryUseCases.updateCategory(categoryId, categoryMapper.toDomain(category))
                .map(categoryMapper::toResponseDto);
    }

    @PostMapping()
    public Mono<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto category) {
        return categoryUseCases.createCategory(categoryMapper.toDomain(category))
                .map(categoryMapper::toResponseDto);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Map<String, String>>> deleteCategory(@Valid @PathVariable("categoryId") UUID categoryId) {
        return categoryUseCases.deleteCategory(categoryId)
                .thenReturn(ResponseEntity.ok(Map.of("status", "deleted", "id", categoryId.toString())));
    }
}
