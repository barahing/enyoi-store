package com.store.orders_microservice.application.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.store.orders_microservice.application.exception.OrderNotFoundException;
import com.store.orders_microservice.domain.factory.OrderFactory;
import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.ports.in.IOrderUseCases;
import com.store.orders_microservice.domain.ports.out.IOrderPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderUseCases{
    private final IOrderPersistencePort persistence;


    @Override
    public Mono<Order> createOrder(Order order) {
        Order newOrder = OrderFactory.createNew(order.getClientId(), order.getItems());
        return persistence.saveOrder(newOrder);
    }

    @Override
    public Mono<Order> getOrderById(UUID id) {
        return persistence.findOrderById(id)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(id)));
    }

    @Override
    public Flux<Order> getAllOrders() {
        return persistence.findAllOrders();
    }

    @Override
    public Mono<Order> modifyOrder(UUID id, Order order) {

        return persistence.findOrderById(id)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
            .flatMap(existingOrder -> {
                Order modifiedOrder = OrderFactory.modifyExisting(existingOrder, order);
                return persistence.updateOrder(modifiedOrder);
                });
    }

    @Override
    public Mono<Order> updateOrder(UUID id, Order order) {
        return persistence.findOrderById(id)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
            .flatMap(existingOrder -> {
                existingOrder.setClientId(order.getClientId());
                existingOrder.setItems(order.getItems());
                existingOrder.recalculateTotal();
                existingOrder.markUpdated();
                return persistence.updateOrder(existingOrder);
            });
    }

    @Override
    public Mono<Void> deleteOrder(UUID id) {
        return persistence.findOrderById(id)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
            .flatMap(existing -> persistence.deleteOrder(id));
    }

    @Override
    public Mono<Order> confirmOrder(UUID id) {
        return persistence.findOrderById(id)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
            .flatMap(order -> {
                order.confirm();
                return persistence.updateOrder(order);
            });
    }

    @Override
    public Mono<Order> shipOrder(UUID id) {
        return persistence.findOrderById(id)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
            .flatMap(order -> {
                order.ship();
                return persistence.updateOrder(order);
            });
    }

    @Override
    public Mono<Order> deliverOrder(UUID id) {
        return persistence.findOrderById(id)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
            .flatMap(order -> {
                order.deliver();
                return persistence.updateOrder(order);
            });
    }

    @Override
    public Mono<Order> cancelOrder(UUID id) {
        return persistence.findOrderById(id)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(id)))
            .flatMap(order -> {
                order.cancel();
                return persistence.updateOrder(order);
            });
    }

    

}
