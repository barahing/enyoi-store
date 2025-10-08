package com.store.products_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.products_microservice.infrastructure.persistence.entity.ProductEntity;

public interface IProductR2dbcRepository extends ReactiveCrudRepository<ProductEntity, UUID>{

}
