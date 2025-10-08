package com.store.products_microservice.domain.ports.in;

import java.util.UUID;

import com.store.products_microservice.domain.model.Category;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICategoryUseCases {
    Mono<Category> createCategory(Category category);
    Mono<Category> getCategoryById(UUID id);
    Flux<Category> getAllCategory();
    Mono<Category> updateCategory(UUID id, Category category);
    Mono<Void> deleteCategory (UUID id);
}
