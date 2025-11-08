package com.store.inventory_microservice.application.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.store.common.dto.ProductStockDTO;
import com.store.common.events.StockReservationFailedEvent;
import com.store.common.events.StockReservedEvent;
import com.store.inventory_microservice.domain.exception.ProductAlreadyExistsException;
import com.store.inventory_microservice.domain.exception.ProductCatalogMismatchException;
import com.store.inventory_microservice.domain.exception.StockNotFoundException;
import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.domain.model.StockReservation;
import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;
import com.store.inventory_microservice.domain.ports.out.IEventPublisherPort;
import com.store.inventory_microservice.domain.ports.out.IProductCatalogPort;
import com.store.inventory_microservice.domain.ports.out.IProductStockPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductStockService implements IProductStockServicePort {

    private final IProductStockPersistencePort persistencePort;
    private final IEventPublisherPort eventPublisherPort;
    private final IProductCatalogPort productCatalogPort;

    @Override
    public Mono<ProductStock> getStockByProductId(UUID productId) {
        return persistencePort.findByProductId(productId)
            .switchIfEmpty(Mono.error(new StockNotFoundException("Stock not found for Product ID: " + productId)));
    }

    @Override
    public Mono<ProductStock> createInitialStock(UUID productId, int initialStock) {
        return productCatalogPort.productExists(productId)
            .filter(Boolean::booleanValue)
            .switchIfEmpty(Mono.error(new ProductCatalogMismatchException(productId, "Creation request failed")))
            .flatMap(exists -> 
                persistencePort.findByProductId(productId)
                    .flatMap(existingStock -> {
                        log.warn("Attempt to create stock for existing product: {}", productId);
                        // **CLAVE para el 409 CONFLICT (el Mono.error con el tipo explícito)**
                        return Mono.<ProductStock>error(new ProductAlreadyExistsException(productId));
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        ProductStock newStock = ProductStock.create(productId, initialStock); 
                        return persistencePort.create(newStock);
                    }))
            );
    }
    
    @Override
    public Mono<ProductStock> updateInitialStock(UUID productId, int newInitialStock) {
        return productCatalogPort.productExists(productId)
            .filter(Boolean::booleanValue)
            .switchIfEmpty(Mono.error(new ProductCatalogMismatchException(productId, "Update request failed")))
            .flatMap(exists -> 
                persistencePort.findByProductId(productId)
                    .switchIfEmpty(Mono.error(new StockNotFoundException("Cannot update stock: Stock does not exist for Product ID: " + productId)))
                    .flatMap(existingStock -> {
                        existingStock.setCurrentStock(newInitialStock);
                        return persistencePort.update(existingStock);
                    })
            );
    }
    
    @Override
    public Mono<Void> deductStock(UUID productId, int quantity) {
        return Mono.empty(); 
    }

    @Override
    public Mono<Void> processOrderCreation(UUID orderId, List<ProductStockDTO> products) {
        
        Flux<StockReservation> reservationFlux = Flux.fromIterable(products)
            .flatMap(productDto -> 
                productCatalogPort.productExists(productDto.productId())
                    .filter(Boolean::booleanValue)
                    .switchIfEmpty(Mono.error(new ProductCatalogMismatchException(productDto.productId(), "Order product not found")))
                    .flatMap(exists -> 
                        persistencePort.findByProductId(productDto.productId())
                            .switchIfEmpty(Mono.error(new StockNotFoundException("Product not found in stock: " + productDto.productId())))
                            .flatMap(stock -> {
                                if (stock.canReserve(productDto.quantity())) {
                                    stock.reserveStock(productDto.quantity()); 
                                    
                                    StockReservation reservation = StockReservation.create(
                                        orderId, 
                                        stock.getProductId(), 
                                        productDto.quantity()
                                    );
                                    
                                    return persistencePort.update(stock).then(persistencePort.saveReservation(reservation));
                                } else {
                                    return Mono.error(new RuntimeException("Insufficient stock for product " + productDto.productId()));
                                }
                            })
                    )
            );

        return reservationFlux
            .collectList()
            .flatMap(reservations -> {
                StockReservedEvent successEvent = new StockReservedEvent(orderId);
                return eventPublisherPort.publishStockReservedEvent(successEvent);
            })
            .onErrorResume(e -> {
                log.error("Stock reservation failed for Order ID {}: {}", orderId, e.getMessage());
                
                StockReservationFailedEvent failureEvent = new StockReservationFailedEvent(orderId, e.getMessage());
                
                return persistencePort.findReservationsByOrderId(orderId)
                    .flatMap(persistencePort::deleteReservation)
                    .then(eventPublisherPort.publishStockReservationFailedEvent(failureEvent));
            })
            .then();
    }

    @Override
    public Mono<Void> releaseOrderStock(UUID orderId, List<ProductStockDTO> products) {
        log.warn("Releasing stock for Order ID: {}", orderId);
        
        return persistencePort.findReservationsByOrderId(orderId)
            .flatMap(reservation -> 
                persistencePort.findByProductId(reservation.getProductId())
                    .flatMap(stock -> {
                        stock.releaseReservedStock(reservation.getQuantity());
                        
                        return persistencePort.update(stock)
                            .then(persistencePort.deleteReservation(reservation));
                    })
            )
            .then()
            .doOnError(e -> log.error("Error during stock release for Order {}: {}", orderId, e.getMessage()));
    }

    @Override
    public Mono<Void> confirmStockReservation(UUID orderId) {
        log.info("Confirming stock reservations for Order ID: {}", orderId);
        
        return persistencePort.findReservationsByOrderId(orderId)
            .flatMap(reservation -> {
                
                return persistencePort.findByProductId(reservation.getProductId())
                    .flatMap(stock -> {
                        stock.confirmReservation(reservation.getQuantity());
                        
                        return persistencePort.update(stock)
                            .then(persistencePort.deleteReservation(reservation));
                    });
            })
            .then()
            .doOnError(e -> log.error("Error confirming stock reservation for Order {}: {}", orderId, e.getMessage()));
    }

    @Override
    public Flux<ProductStock> getAllStocks() {
        return persistencePort.findAllStocks();
        
    }
    
    @Override
    public Mono<Boolean> isQuantityAvailable(UUID productId, int quantity) {
        return persistencePort.findByProductId(productId)
            .map(stock -> stock.getCurrentStock() >= quantity)
            .defaultIfEmpty(false);
    }

}