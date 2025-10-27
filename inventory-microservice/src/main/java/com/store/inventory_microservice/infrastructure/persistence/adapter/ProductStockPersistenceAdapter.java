package com.store.inventory_microservice.infrastructure.persistence.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.domain.ports.out.IProductStockRepositoryPort;
import com.store.inventory_microservice.infrastructure.persistence.mapper.IProductStockEntityMapper;
import com.store.inventory_microservice.infrastructure.persistence.repository.IProductStockR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductStockPersistenceAdapter implements IProductStockRepositoryPort {

    private final IProductStockR2dbcRepository repository;
    private final IProductStockEntityMapper mapper;

    @Override
    public Mono<ProductStock> findByProductId(UUID productId) {
        return repository.findById(productId)
            .map(mapper::toDomain);
    }

    @Override
    public Mono<ProductStock> save(ProductStock stock) {
        return repository.save(mapper.toEntity(stock))
            .map(mapper::toDomain);
    }
}