package com.store.orders_microservice.domain.ports.in;

import java.util.UUID;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.store.orders_microservice.domain.model.Order;

public interface IOrderUseCases {

    Mono<Order> createOrder(Order order);
    Mono<Order> getOrderById(UUID id);
    Flux<Order> getAllOrders();
    Mono<Order> modifyOrder(UUID id, Order updatedOrder);
    Mono<Order> updateOrder(UUID id, Order updatedOrder);
    Mono<Void> deleteOrder(UUID id);
    Mono<Order> confirmOrder(UUID id);
    Mono<Order> shipOrder(UUID id);
    Mono<Order> deliverOrder(UUID id);
    Mono<Order> cancelOrder(UUID id);
}
