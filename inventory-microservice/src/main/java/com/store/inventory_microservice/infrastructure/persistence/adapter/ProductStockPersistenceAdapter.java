package com.store.inventory_microservice.infrastructure.persistence.adapter;

import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.store.inventory_microservice.domain.exception.DuplicateReservationException;
import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.domain.model.StockReservation;
import com.store.inventory_microservice.domain.ports.out.IProductStockPersistencePort;
import com.store.inventory_microservice.infrastructure.persistence.entity.ProductStockEntity;
import com.store.inventory_microservice.infrastructure.persistence.entity.StockReservationEntity;
import com.store.inventory_microservice.infrastructure.persistence.mapper.IProductStockMapper;
import com.store.inventory_microservice.infrastructure.persistence.mapper.IStockReservationMapper;
import com.store.inventory_microservice.infrastructure.persistence.repository.IProductStockR2dbcRepository;
import com.store.inventory_microservice.infrastructure.persistence.repository.IStockReservationR2dbcRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductStockPersistenceAdapter implements IProductStockPersistencePort {

    private final IProductStockR2dbcRepository stockRepository;
    private final IStockReservationR2dbcRepository reservationRepository;
    private final IProductStockMapper stockMapper;
    private final IStockReservationMapper reservationMapper;

    // ---------------------------------------------------------------------
    // PRODUCT STOCK
    // ---------------------------------------------------------------------

    @Override
    @Transactional
    public Mono<ProductStock> create(ProductStock stock) {
        ProductStockEntity entity = ProductStockEntity.newEntity(
            stock.getProductId(),
            stock.getCurrentStock(),
            stock.getReservedStock()
        );

        return stockRepository.save(entity)
            .map(stockMapper::toDomain)
            .doOnNext(saved -> log.debug("✅ Created ProductStock for product {}", saved.getProductId()));
    }

    @Override
    @Transactional
    public Mono<ProductStock> update(ProductStock stock) {
        ProductStockEntity entity = stockMapper.toEntity(stock);

        return stockRepository.save(entity)
            .map(stockMapper::toDomain)
            .doOnNext(saved -> log.debug("🟢 Updated ProductStock for product {}", saved.getProductId()));
    }

    @Override
    public Mono<ProductStock> findByProductId(UUID productId) {
        return stockRepository.findById(productId)
            .map(stockMapper::toDomain);
    }

    @Override
    public Flux<ProductStock> findAllStocks() {
        return stockRepository.findAll()
            .map(stockMapper::toDomain);
    }

    // ---------------------------------------------------------------------
    // STOCK RESERVATIONS
    // ---------------------------------------------------------------------

    @Override
    @Transactional
    public Mono<StockReservation> createReservation(StockReservation reservation) {
        StockReservationEntity entity = reservationMapper.toEntity(reservation);
        entity.setId(null); // asegura INSERT

        return reservationRepository.save(entity)
            .map(reservationMapper::toDomain)
            .doOnNext(saved ->
                log.debug("✅ Created StockReservation [order={}, product={}, id={}]",
                    saved.getOrderId(), saved.getProductId(), saved.getId())
            )
            .onErrorResume(e -> {
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("stock_reservations_order_id_product_id_key")
                        || e instanceof DuplicateKeyException) {
                    log.warn("♻️ Duplicate reservation detected for order {} and product {}",
                            reservation.getOrderId(), reservation.getProductId());
                    return Mono.error(new DuplicateReservationException(
                            reservation.getOrderId(), reservation.getProductId()));
                }
                return Mono.error(e);
            });
    }

    @Override
    @Transactional
    public Mono<StockReservation> updateReservation(StockReservation reservation) {
        if (reservation.getId() == null) {
            return Mono.error(new IllegalArgumentException("Cannot update reservation without ID"));
        }

        StockReservationEntity entity = reservationMapper.toEntity(reservation);

        return reservationRepository.save(entity)
            .map(reservationMapper::toDomain)
            .doOnNext(saved -> log.debug("🟢 Updated StockReservation {}", saved.getId()));
    }

    @Override
    public Flux<StockReservation> findReservationsByOrderId(UUID orderId) {
        return reservationRepository.findByOrderId(orderId)
            .map(reservationMapper::toDomain);
    }

    @Override
    @Transactional
    public Mono<Void> deleteReservation(StockReservation reservation) {
        StockReservationEntity entity = reservationMapper.toEntity(reservation);
        return reservationRepository.delete(entity)
            .doOnSuccess(v -> log.debug("🗑️ Deleted StockReservation for product {}", reservation.getProductId()));
    }
}
