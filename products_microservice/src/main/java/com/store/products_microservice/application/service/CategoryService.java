package com.store.products_microservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.store.products_microservice.domain.exception.CategoryNotFoundException;
import com.store.products_microservice.domain.model.Category;
import com.store.products_microservice.domain.ports.in.ICategoryUseCases;
import com.store.products_microservice.domain.ports.out.ICategoryPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryUseCases{
    private final ICategoryPersistencePort persistence;
    
    @Override
    public Mono<Category> createCategory(Category category) {
        return persistence.saveCategory(category);
    }

    @Override
    public Mono<Category> getCategoryById(UUID id) {
        return persistence.findCategoryById(id)
            .switchIfEmpty(Mono.error(new CategoryNotFoundException(id)));
    }

    @Override
    public Flux<Category> getAllCategory() {
        return persistence.findAllCategorys();
    }

    @Override
    public Mono<Category> updateCategory(UUID id, Category category) {
        return persistence.findCategoryById(id)
            .switchIfEmpty(Mono.error(new CategoryNotFoundException(id)))
            .flatMap(existingCategory -> {
                Category updatedCategory = new Category (
                    existingCategory.id(),
                    category.name() != null ? category.name() : existingCategory.name(),
                    category.description() != null ? category.description() : existingCategory.description()
                );
                return persistence.updateCategory(id, updatedCategory);
            });
    }

    @Override
    public Mono<Void> deleteCategory(UUID id) {
        return persistence.findCategoryById(id)
            .switchIfEmpty(Mono.error(new CategoryNotFoundException(id)))
            .flatMap(u -> persistence.deleteCategory(id));
    }

}
