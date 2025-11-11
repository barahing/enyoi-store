package com.store.inventory_microservice.domain.ports.out;

import java.util.UUID;

import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.domain.model.StockReservation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductStockPersistencePort {
    
    Flux<ProductStock> findAllStocks();
    Mono<ProductStock> create(ProductStock stock);
    Mono<ProductStock> update(ProductStock stock);
    Mono<ProductStock> findByProductId(UUID productId);
    Mono<ProductStock> increaseStock(UUID productId, Integer quantity);
    Mono<StockReservation> createReservation(StockReservation reservation);
    Mono<StockReservation> updateReservation(StockReservation reservation);
    Flux<StockReservation> findReservationsByOrderId(UUID orderId);
    Mono<Void> deleteReservation(StockReservation reservation);
}
