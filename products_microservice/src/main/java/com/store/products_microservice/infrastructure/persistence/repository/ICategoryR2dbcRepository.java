package com.store.products_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.products_microservice.infrastructure.persistence.entity.CategoryEntity;

public interface ICategoryR2dbcRepository extends ReactiveCrudRepository<CategoryEntity, UUID>{

}
