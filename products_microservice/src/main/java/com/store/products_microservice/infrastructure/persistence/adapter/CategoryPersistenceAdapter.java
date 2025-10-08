package com.store.products_microservice.infrastructure.persistence.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.store.products_microservice.domain.model.Category;
import com.store.products_microservice.domain.ports.out.ICategoryPersistencePort;
import com.store.products_microservice.infrastructure.persistence.mapper.CategoryMapperEntity;
import com.store.products_microservice.infrastructure.persistence.repository.ICategoryR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements ICategoryPersistencePort {
    private final ICategoryR2dbcRepository categoryRepository;
    private final CategoryMapperEntity mapper;

    
    public Mono<Category> saveCategory(Category product) {
        return categoryRepository.save(mapper.toEntity(product)).map(mapper::toDomain);
    }

    @Override
    public Mono<Category> findCategoryById(UUID id) {
        return categoryRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<Category> findAllCategorys() {
        return categoryRepository.findAll().map(mapper::toDomain);
    }

    @Override
    public Mono<Category> updateCategory(UUID id, Category product) {
        return categoryRepository.save(mapper.toEntity(product)).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteCategory(UUID id) {
        return categoryRepository.deleteById(id);
    }

}

