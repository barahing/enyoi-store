package com.store.orders_microservice.domain.ports.out;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.store.orders_microservice.domain.model.Order;

public interface IOrderRepositoryPort {

    Mono<Order> save(Order order);
    Mono<Order> findById(UUID id);
    Flux<Order> findAll();
    //Mono<Order> updateOrder(Order order);
    //Mono<Void> deleteById(UUID id);
}
