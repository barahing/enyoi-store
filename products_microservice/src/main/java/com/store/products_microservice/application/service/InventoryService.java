package com.store.products_microservice.application.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.store.products_microservice.domain.exception.ProductNotFoundException;
import com.store.products_microservice.domain.model.Product;
import com.store.products_microservice.domain.ports.in.IInventoryUseCases;
import com.store.products_microservice.domain.ports.out.IProductPersistencePort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InventoryService implements IInventoryUseCases {

    private final IProductPersistencePort persistence;

    @Override
    public Mono<Void> increaseStock(UUID productId, int quantity) {
        return persistence.findProductById(productId)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
            .flatMap(product -> {
                Product updated = product.increaseStock(quantity);
                return persistence.updateProduct(productId, updated).then();
            });
    }

    @Override
    public Mono<Void> decreaseStock(UUID productId, int quantity) {
        return persistence.findProductById(productId)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
            .flatMap(product -> {
                Product updated = product.decreaseStock(quantity);
                return persistence.updateProduct(productId, updated).then();
            });
    }

    @Override
    public Mono<Boolean> isInStock(UUID productId, int requiredQuantity) {
        return persistence.findProductById(productId)
            .map(product -> product.stock() >= requiredQuantity)
            .defaultIfEmpty(false);
    }
}
