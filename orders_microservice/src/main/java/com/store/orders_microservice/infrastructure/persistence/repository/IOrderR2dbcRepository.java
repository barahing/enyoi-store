package com.store.orders_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.orders_microservice.infrastructure.entity.OrderEntity;

public interface IOrderR2dbcRepository extends ReactiveCrudRepository<OrderEntity, UUID> {

}
