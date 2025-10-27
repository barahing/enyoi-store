package com.store.inventory_microservice.domain.ports.in;

import java.util.UUID;
import com.store.inventory_microservice.domain.model.ProductStock;
import reactor.core.publisher.Mono;

public interface IProductStockServicePort {

    Mono<ProductStock> getStockByProductId(UUID productId);
    Mono<ProductStock> createInitialStock(UUID productId, int initialStock);
    Mono<Void> reserveStock(UUID productId, int quantity);
    Mono<Void> releaseStock(UUID productId, int quantity);
    Mono<Void> deductStock(UUID productId, int quantity);
}