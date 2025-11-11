package com.store.inventory_microservice.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store.common.dto.ProductStockDTO;
import com.store.common.events.StockReservationFailedEvent;
import com.store.common.events.StockReservedEvent;
import com.store.inventory_microservice.domain.exception.DuplicateReservationException;
import com.store.inventory_microservice.domain.exception.ProductAlreadyExistsException;
import com.store.inventory_microservice.domain.exception.ProductCatalogMismatchException;
import com.store.inventory_microservice.domain.exception.StockNotFoundException;
import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.domain.model.StockReservation;
import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;
import com.store.inventory_microservice.domain.ports.out.IEventPublisherPort;
import com.store.inventory_microservice.domain.ports.out.IProductCatalogPort;
import com.store.inventory_microservice.domain.ports.out.IProductStockPersistencePort;
import com.store.inventory_microservice.infrastructure.persistence.repository.StockHistoryRepository;

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
    private final StockHistoryRepository historyRepository;

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
                    .flatMap(existing -> Mono.<ProductStock>error(new ProductAlreadyExistsException(productId)))
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
                                if (!stock.canReserve(productDto.quantity())) {
                                    return Mono.error(new RuntimeException("Insufficient stock for product " + productDto.productId()));
                                }

                                stock.reserveStock(productDto.quantity());
                                StockReservation reservation = StockReservation.create(orderId, stock.getProductId(), productDto.quantity());

                                return persistencePort.findReservationsByOrderId(orderId)
                                    .filter(existing -> existing.getProductId().equals(stock.getProductId()))
                                    .hasElements()
                                    .flatMap(existsReservation -> {
                                        if (existsReservation) {
                                            log.warn("♻️ Reservation already exists for order {} and product {}, skipping.", orderId, stock.getProductId());
                                            return Mono.empty();
                                        }

                                        return persistencePort.update(stock)
                                            .then(persistencePort.createReservation(reservation)
                                                .onErrorResume(e -> {
                                                    String msg = e.getMessage() != null ? e.getMessage() : "";
                                                    if (msg.contains("stock_reservations_order_id_product_id_key")) {
                                                        log.warn("♻️ Duplicate reservation for order {} and product {}, skipping.", orderId, stock.getProductId());
                                                        return Mono.empty();
                                                    }
                                                    if (e instanceof DuplicateReservationException) {
                                                        log.warn("♻️ Duplicate reservation detected concurrently, skipping insert.");
                                                        return Mono.empty();
                                                    }
                                                    return Mono.error(e);
                                                }));
                                    });
                            })
                    )
            );

        return reservationFlux
            .collectList()
            .flatMap(reservations -> {
                if (reservations.isEmpty()) {
                    log.info("No new stock reservations created for Order {}", orderId);
                    return Mono.empty();
                }

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
                            .then(persistencePort.deleteReservation(reservation))
                            .then(recordHistory(stock.getProductId(), "RELEASED", reservation.getQuantity(), orderId.toString()));
                    })
            )
            .then()
            .doOnSuccess(v -> log.info("✅ Stock released for Order {}", orderId))
            .doOnError(e -> log.error("Error during stock release for Order {}: {}", orderId, e.getMessage()));
    }

    @Override
    public Mono<Void> confirmStockReservation(UUID orderId) {
        log.info("Confirming stock reservations for Order ID: {}", orderId);

        return persistencePort.findReservationsByOrderId(orderId)
            .flatMap(reservation ->
                persistencePort.findByProductId(reservation.getProductId())
                    .flatMap(stock -> {
                        stock.confirmReservation(reservation.getQuantity());
                        return persistencePort.update(stock)
                            .then(persistencePort.deleteReservation(reservation))
                            .then(recordHistory(stock.getProductId(), "DEDUCTED", reservation.getQuantity(), orderId.toString()));
                    })
            )
            .then()
            .doOnSuccess(v -> log.info("✅ Stock confirmed for Order {}", orderId))
            .doOnError(e -> log.error("Error confirming stock reservation for Order {}: {}", orderId, e.getMessage()));
    }

    @Override
    public Mono<Void> confirmStockForOrder(UUID orderId) {
        log.info("🔄 [INVENTORY] confirmStockForOrder START for order: {}", orderId);

        return persistencePort.findReservationsByOrderId(orderId)
            .doOnNext(reservation -> log.info("📦 Found reservation: order={}, product={}, quantity={}", 
                    reservation.getOrderId(), reservation.getProductId(), reservation.getQuantity()))
            .flatMap(reservation -> persistencePort.findByProductId(reservation.getProductId())
                .flatMap(stock -> {
                    stock.confirmReservation(reservation.getQuantity());
                    return persistencePort.update(stock)
                        .then(recordHistory(stock.getProductId(), "DEDUCTED", reservation.getQuantity(), orderId.toString()))
                        .then(persistencePort.deleteReservation(reservation));
                })
            )
            .then()
            .doOnSuccess(v -> log.info("✅ [INVENTORY] confirmStockForOrder COMPLETED for order: {}", orderId))
            .doOnError(e -> log.error("❌ [INVENTORY] confirmStockForOrder FAILED for order {}: {}", orderId, e.getMessage(), e));
    }

    @Override
    public Flux<ProductStock> getAllStocks() {
        return persistencePort.findAllStocks();
    }

    @Override
    public Mono<Boolean> isQuantityAvailable(UUID productId, int quantity) {
        return persistencePort.findByProductId(productId)
            .map(stock -> stock.canReserve(quantity))
            .defaultIfEmpty(false);
    }

    private Mono<Void> recordHistory(UUID productId, String action, int quantity, String reference) {
        return historyRepository.save(
                new com.store.inventory_microservice.domain.model.StockHistory(
                    UUID.randomUUID(),
                    productId,
                    action,
                    quantity,
                    reference,
                    LocalDateTime.now()
                )
            )
            .doOnSuccess(h -> log.info("🧾 [INVENTORY] Stock history recorded: {} x{} for product {}", action, quantity, productId))
            .then();
    }

    @Transactional
    public Mono<ProductStock> increaseStock(UUID productId, Integer quantity, UUID purchaseOrderId) {
        log.info("📈 [SERVICE] Increasing stock for product {} by {} units (Purchase Order: {})", 
                productId, quantity, purchaseOrderId);
        
        if (quantity <= 0) {
            return Mono.error(new IllegalArgumentException("Quantity must be greater than 0"));
        }

        // ⚙️ 1️⃣ Verificamos si ya se registró este evento (idempotencia)
        return historyRepository.findByProductId(productId)
            .filter(h -> "RECEIVED".equalsIgnoreCase(h.getAction()) && purchaseOrderId.toString().equals(h.getReference()))
            .hasElements()
            .flatMap(alreadyProcessed -> {
                if (alreadyProcessed) {
                    log.warn("♻️ [SERVICE] Duplicate StockReceivedEvent detected for product {}, skipping increase.", productId);
                    return persistencePort.findByProductId(productId); // devolvemos el estado actual sin modificar
                }

                // ⚙️ 2️⃣ Ejecutamos el aumento real de stock
                return persistencePort.increaseStock(productId, quantity)
                    .flatMap(updatedStock ->
                        // ⚙️ 3️⃣ Registramos en el histórico
                        historyRepository.save(
                            new com.store.inventory_microservice.domain.model.StockHistory(
                                UUID.randomUUID(),
                                productId,
                                "RECEIVED",
                                quantity,
                                purchaseOrderId.toString(),
                                LocalDateTime.now()
                            )
                        )
                        .thenReturn(updatedStock)
                    )
                    .doOnSuccess(updated ->
                        log.info("✅ [SERVICE] Stock increased successfully for product {} to {} units (Purchase Order: {})",
                                productId, updated.getCurrentStock(), purchaseOrderId)
                    )
                    .doOnError(error ->
                        log.error("❌ [SERVICE] Failed to increase stock for product {}: {}", productId, error.getMessage())
                    );
            });
    }
}
