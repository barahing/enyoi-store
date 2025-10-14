package com.store.orders_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.store.orders_microservice.infrastructure.entity.OrderItemEntity;

import reactor.core.publisher.Flux;

public interface IOrderItemR2dbcRepository extends ReactiveCrudRepository<OrderItemEntity, UUID> {
    Flux<OrderItemEntity> findByOrderId(UUID orderId);

}
