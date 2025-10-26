package com.store.orders_microservice.application.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.store.common.events.CartConvertedEvent;
import com.store.orders_microservice.domain.factory.OrderFactory; 
import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.model.OrderItem;
import com.store.orders_microservice.domain.ports.in.IOrderServicePort;
import com.store.orders_microservice.domain.ports.out.IOrderRepositoryPort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderServicePort {

    private final IOrderRepositoryPort orderRepository;

    @Override
    public Mono<Order> createOrderFromCart(CartConvertedEvent event) {
        List<OrderItem> items = event.items().stream()
                .map(itemData -> OrderItem.create( 
                    itemData.productId(),
                    itemData.quantity(),
                    itemData.price()
                ))
                .collect(Collectors.toList());

        Order newOrder = OrderFactory.createNew( 
            event.clientId(),
            items
        );
        
        return orderRepository.save(newOrder);
    }

    @Override
    public Mono<Order> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Order not found with ID: " + orderId)));
    }

    @Override
    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    private Mono<Order> updateOrderStatus(UUID orderId, java.util.function.Consumer<Order> statusUpdater) {
        return getOrderById(orderId)
                .doOnNext(statusUpdater) 
                .flatMap(orderRepository::save);
    }
    
    @Override
    public Mono<Order> confirmOrder(UUID orderId) {
        return updateOrderStatus(orderId, Order::confirm);
    }

    @Override
    public Mono<Order> shipOrder(UUID orderId) {
        return updateOrderStatus(orderId, Order::ship);
    }

    @Override
    public Mono<Order> deliverOrder(UUID orderId) {
        return updateOrderStatus(orderId, Order::deliver);
    }

    @Override
    public Mono<Order> cancelOrder(UUID orderId) {
        return updateOrderStatus(orderId, Order::cancel);
    }
    
    @Override
    public Mono<Order> addProductToOrder(UUID orderId, OrderItem item) {
        return getOrderById(orderId)
                .doOnNext(order -> {
                    if (!order.isModifiable()) {
                        throw new IllegalStateException("Order " + orderId + " cannot be modified (Status: " + order.getStatus() + ").");
                    }
                    order.addItem(item);
                })
                .flatMap(orderRepository::save);
    }

    @Override
    public Mono<Order> removeProductFromOrder(UUID orderId, UUID productId) {
        return getOrderById(orderId)
                .doOnNext(order -> {
                    if (!order.isModifiable()) {
                        throw new IllegalStateException("Order " + orderId + " cannot be modified (Status: " + order.getStatus() + ").");
                    }
                    order.removeItem(productId);
                })
                .flatMap(orderRepository::save);
    }

    @Override
    public Mono<Order> updateItemQuantity(UUID orderId, UUID productId, int newQuantity) {
        return getOrderById(orderId)
                .doOnNext(order -> {
                    if (!order.isModifiable()) {
                        throw new IllegalStateException("Order " + orderId + " cannot be modified (Status: " + order.getStatus() + ").");
                    }
                    order.updateItemQuantity(productId, newQuantity);
                })
                .flatMap(orderRepository::save);
    }
}