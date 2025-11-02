package com.store.products_microservice.infrastructure.persistence.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.store.products_microservice.domain.model.Product;
import com.store.products_microservice.domain.ports.out.IProductRepositoryPort; 
import com.store.products_microservice.infrastructure.persistence.mapper.ProductMapperEntity;
import com.store.products_microservice.infrastructure.persistence.repository.IProductR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements IProductRepositoryPort { 
    private final IProductR2dbcRepository productRepository;
    private final ProductMapperEntity mapper;

    @Override
    public Mono<Product> save(Product product) {
        return productRepository.save(mapper.toEntity(product)).map(mapper::toDomain);
    }

    @Override
    public Mono<Product> findById(UUID id) {
        return productRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<Product> findAll() {
        return productRepository.findAll().map(mapper::toDomain);
    }

    @Override
    public Mono<Product> update(UUID id, Product product) {
        return productRepository.save(mapper.toEntity(product)).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return productRepository.deleteById(id);
    }

    @Override
    public Flux<Product> findByIds(List<UUID> ids) {
        return productRepository.findAllById(ids).map(mapper::toDomain);
    }

    @Override
    public Flux<Product> saveAll(Flux<Product> products) {
        return productRepository.saveAll(products.map(mapper::toEntity)).map(mapper::toDomain);
    }
}