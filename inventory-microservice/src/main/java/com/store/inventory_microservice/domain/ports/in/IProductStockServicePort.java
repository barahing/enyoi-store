package com.store.inventory_microservice.domain.ports.in;

import java.util.List;
import java.util.UUID;

import com.store.common.dto.ProductStockDTO;
import com.store.inventory_microservice.domain.model.ProductStock;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductStockServicePort {

    Flux<ProductStock> getAllStocks();
    Mono<ProductStock> getStockByProductId(UUID productId);
    Mono<ProductStock> createInitialStock(UUID productId, int initialStock);
    Mono<Void> deductStock(UUID productId, int quantity);
    Mono<ProductStock> updateInitialStock(UUID productId, int initialStock);
    Mono<Void> processOrderCreation(UUID orderId, List<ProductStockDTO> products);
    Mono<Void> releaseOrderStock(UUID orderId, List<ProductStockDTO> products);
    Mono<Void> confirmStockReservation(UUID orderId);
    Mono<Boolean> isQuantityAvailable(UUID productId, int quantity);
    Mono<Void> confirmStockForOrder(UUID orderId);

}