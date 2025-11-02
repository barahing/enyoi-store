package com.store.inventory_microservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.store.common.dto.ProductStockDTO;
import com.store.common.events.StockReservationFailedEvent;
import com.store.common.events.StockReservedEvent;
import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;
import com.store.inventory_microservice.domain.ports.out.IEventPublisherPort;
import com.store.inventory_microservice.domain.ports.out.IProductStockRepositoryPort;
import com.store.inventory_microservice.domain.exception.NotEnoughStockException;
import com.store.inventory_microservice.application.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductStockService implements IProductStockServicePort {

    private final IProductStockRepositoryPort repositoryPort;
    private final IEventPublisherPort eventPublisher; 
    
    private Mono<ProductStock> findStock(UUID productId) {
        return repositoryPort.findByProductId(productId)
            .switchIfEmpty(Mono.error(
                new ResourceNotFoundException("Stock not found for product ID: " + productId)
            ));
    }
    
    @Transactional
    @Override
    public Mono<Void> processOrderCreation(UUID orderId, List<ProductStockDTO> products) {
        
        Flux<ProductStock> reservationFlux = Flux.fromIterable(products)
            .flatMap(dto -> findStock(dto.productId())
                .doOnNext(stock -> {
                    try {
                        stock.reserveStock(dto.quantity());
                    } catch (IllegalStateException e) {
                        throw new NotEnoughStockException(dto.productId(), stock.getAvailableStock(), dto.quantity());
                    }
                })
            );
        
        Mono<List<ProductStock>> saveAllMono = reservationFlux
            .flatMap(repositoryPort::save)
            .collectList();
            
        return saveAllMono
            .then(
                eventPublisher.publishStockReservedEvent(new StockReservedEvent(orderId))
            )
            .onErrorResume(e -> {
                String reason = String.format("Stock reservation failed for Order %s: %s", orderId, e.getMessage());
                return eventPublisher.publishStockReservationFailedEvent(
                    new StockReservationFailedEvent(orderId, reason)
                );
            });
    }

    @Transactional
    @Override
    public Mono<Void> releaseOrderStock(UUID orderId, List<ProductStockDTO> products) {
        
        Flux<ProductStock> releaseFlux = Flux.fromIterable(products)
            .flatMap(dto -> findStock(dto.productId())
                .doOnNext(stock -> {
                    try {
                        stock.releaseReservedStock(dto.quantity());
                    } catch (IllegalStateException e) {
                    }
                })
            );
            
        return releaseFlux
            .flatMap(repositoryPort::save)
            .then();
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