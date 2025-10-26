package com.store.orders_microservice.domain.ports.in;

import java.util.UUID;
import com.store.common.events.CartConvertedEvent;
import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.model.OrderItem; // Necesitas importar OrderItem

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IOrderServicePort {

    Mono<Order> createOrderFromCart(CartConvertedEvent event);
    Mono<Order> getOrderById(UUID orderId);
    Flux<Order> getAllOrders();
    Mono<Order> confirmOrder(UUID orderId);
    Mono<Order> shipOrder(UUID orderId);
    Mono<Order> deliverOrder(UUID orderId);
    Mono<Order> cancelOrder(UUID orderId); 
    Mono<Order> addProductToOrder(UUID orderId, OrderItem item);
    Mono<Order> removeProductFromOrder(UUID orderId, UUID productId);
    Mono<Order> updateItemQuantity(UUID orderId, UUID productId, int newQuantity);
    //Mono<Void> deleteOrder(UUID id);
}