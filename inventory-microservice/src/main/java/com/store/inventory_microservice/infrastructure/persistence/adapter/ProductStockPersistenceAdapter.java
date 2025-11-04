package com.store.inventory_microservice.infrastructure.persistence.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductStockPersistenceAdapter implements IProductStockPersistencePort {

    private final IProductStockR2dbcRepository stockRepository;
    private final IStockReservationR2dbcRepository reservationRepository;
    private final IProductStockMapper stockMapper;
    private final IStockReservationMapper reservationMapper;

    @Override
    @Transactional
    public Mono<ProductStock> create(ProductStock stock) {
        ProductStockEntity entity = ProductStockEntity.newEntity(
        stock.getProductId(),
        stock.getCurrentStock(),
        stock.getReservedStock()
        );

        return stockRepository.save(entity)
        .map(stockMapper::toDomain);
    }

    @Override
    @Transactional
        public Mono<ProductStock> update(ProductStock stock) {
        // Para la actualización, mapeamos normalmente (isNew será false)
        return stockRepository.save(stockMapper.toEntity(stock))
        .map(stockMapper::toDomain);
}

    @Override
    public Mono<ProductStock> findByProductId(UUID productId) {
        return stockRepository.findById(productId)
            .map(stockMapper::toDomain);
    }

    @Override
    public Mono<StockReservation> saveReservation(StockReservation reservation) {
        StockReservationEntity entity = reservationMapper.toEntity(reservation);
        return reservationRepository.save(entity)
            .map(reservationMapper::toDomain);
    }

    @Override
    public Flux<StockReservation> findReservationsByOrderId(UUID orderId) {
        return reservationRepository.findByOrderId(orderId)
            .map(reservationMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteReservation(StockReservation reservation) {
        StockReservationEntity entity = reservationMapper.toEntity(reservation);
        return reservationRepository.delete(entity);
    }

    @Override
    public Flux<ProductStock> findAllStocks() {
        return stockRepository.findAll()
            .map(stockMapper::toDomain);
    }
}