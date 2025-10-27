package com.store.inventory_microservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;
import com.store.inventory_microservice.domain.ports.out.IProductStockRepositoryPort;
import com.store.inventory_microservice.application.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductStockService implements IProductStockServicePort {

    private final IProductStockRepositoryPort repositoryPort;

    private Mono<ProductStock> findStock(UUID productId) {
        return repositoryPort.findByProductId(productId)
            .switchIfEmpty(Mono.error(
                new ResourceNotFoundException("Stock not found for product ID: " + productId)
            ));
    }

    @Override
    public Mono<ProductStock> getStockByProductId(UUID productId) {
        return findStock(productId);
    }

    @Override
    public Mono<ProductStock> createInitialStock(UUID productId, int initialStock) {
        ProductStock newStock = ProductStock.createNew(productId, initialStock);
        return repositoryPort.save(newStock);
    }

    @Override
    public Mono<Void> reserveStock(UUID productId, int quantity) {
        return findStock(productId)
            .flatMap(stock -> {
                try {
                    stock.reserveStock(quantity);
                    return repositoryPort.save(stock);
                } catch (IllegalStateException | IllegalArgumentException e) {
                    return Mono.error(e);
                }
            })
            .then(); 
    }

    @Override
    public Mono<Void> releaseStock(UUID productId, int quantity) {
        return findStock(productId)
            .flatMap(stock -> {
                try {
                    stock.releaseReservedStock(quantity);
                    return repositoryPort.save(stock);
                } catch (IllegalStateException | IllegalArgumentException e) {
                    return Mono.error(e);
                }
            })
            .then();
    }

    @Override
    public Mono<Void> deductStock(UUID productId, int quantity) {
        return findStock(productId)
            .flatMap(stock -> {
                try {
                    stock.deductReservedStock(quantity);
                    return repositoryPort.save(stock);
                } catch (IllegalStateException | IllegalArgumentException e) {
                    return Mono.error(e);
                }
            })
            .then();
    }
}