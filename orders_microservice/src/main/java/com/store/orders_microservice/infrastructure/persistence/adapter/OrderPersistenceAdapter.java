package com.store.orders_microservice.infrastructure.persistence.adapter;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.ports.out.IOrderPersistencePort;
import com.store.orders_microservice.infrastructure.persistence.mapper.OrderMapperEntity;
import com.store.orders_microservice.infrastructure.persistence.repository.IOrederR2dbcRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements IOrderPersistencePort{
    private final OrderMapperEntity mapper;
    private final IOrederR2dbcRepository orderRepository;

   
    @Override
    public Mono<Order> saveOrder(Order order) {
        return orderRepository.save(mapper.toEntity(order)).map(mapper::toDomain);
    }

    @Override
    public Mono<Order> findOrderById(UUID id) {
        return orderRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Flux<Order> findAllOrders() {
        return orderRepository.findAll().map(mapper::toDomain);
    }

    @Override
    public Mono<Order> updateOrder(Order order) {
        return orderRepository.save(mapper.toEntity(order)).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteOrder(UUID id) {
        return orderRepository.deleteById(id);
    }

}
