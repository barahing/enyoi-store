package com.store.products_microservice.domain.factory;

import com.store.products_microservice.domain.model.Category;

public class CategoryFactory {
    public CategoryFactory() {}

    public static Category createNew(String name, String description){
        return new Category(
            null,
            name,
            description
        );
    }
}


