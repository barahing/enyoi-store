package com.store.products_microservice.infrastructure.persistence.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.store.products_microservice.domain.model.Product;
import com.store.products_microservice.domain.ports.out.IProductPersistencePort;
import com.store.products_microservice.infrastructure.persistence.mapper.ProductMapperEntity;
import com.store.products_microservice.infrastructure.persistence.repository.IProductR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductPersistenceAdapter implements IProductPersistencePort {
    private final IProductR2dbcRepository productRepository;
    private final ProductMapperEntity mapper;

    
    public Mono<Product> saveProduct(Product product) {
        return productRepository.save(mapper.toEntity(product)).map(mapper::toDomain);
    }

    @Override
    public Mono<Product> findProductById(UUID id) {
        return productRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<Product> findAllProducts() {
        return productRepository.findAll().map(mapper::toDomain);
    }

    @Override
    public Mono<Product> updateProduct(UUID id, Product product) {
        return productRepository.save(mapper.toEntity(product)).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteProduct(UUID id) {
        return productRepository.deleteById(id);
    }

}
