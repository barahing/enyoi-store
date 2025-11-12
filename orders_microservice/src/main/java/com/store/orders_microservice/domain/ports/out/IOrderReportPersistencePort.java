package com.store.orders_microservice.domain.ports.out;

import com.store.orders_microservice.infrastructure.persistence.entity.OrderEntity;
import com.store.orders_microservice.infrastructure.persistence.entity.OrderItemEntity;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public interface IOrderReportPersistencePort {

    Flux<OrderEntity> findCompletedOrdersBetween(LocalDateTime start, LocalDateTime end);

    Flux<OrderItemEntity> findOrderItemsByOrderIds(Set<UUID> orderIds);
}
