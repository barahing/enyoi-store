package com.store.products_microservice.domain.ports.out;

import java.util.UUID;

import com.store.products_microservice.domain.model.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICategoryPersistencePort {
    Mono<Category> saveCategory(Category category);
    Mono<Category> findCategoryById(UUID id);
    Flux<Category> findAllCategorys();
    Mono<Category> updateCategory(UUID id, Category category);
    Mono<Void> deleteCategory(UUID id);
}
