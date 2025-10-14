package com.store.orders_microservice.domain.ports.out;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.store.orders_microservice.domain.model.Order;

public interface IOrderPersistencePort {

    Mono<Order> saveOrder(Order order);
    Mono<Order> findOrderById(UUID id);
    Flux<Order> findAllOrders();
    Mono<Order> updateOrder(Order order);
    Mono<Void> deleteOrder(UUID id);
}
