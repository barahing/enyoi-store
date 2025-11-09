package com.store.orders_microservice.application.service;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.store.common.events.CartConvertedEvent;
import com.store.common.events.OrderCreatedEvent;
import com.store.common.events.OrderCancelledEvent;
import com.store.common.commands.ProcessPaymentCommand; 
import com.store.common.commands.ReserveStockCommand;
import com.store.common.dto.ProductStockDTO; 
import com.store.orders_microservice.domain.exception.OrderNotFoundException; 
import com.store.orders_microservice.domain.factory.OrderFactory; 
import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.model.OrderItem;
import com.store.orders_microservice.domain.ports.in.IOrderServicePort;
import com.store.orders_microservice.domain.ports.out.IEventPublisherPort;
import com.store.orders_microservice.domain.ports.out.IOrderRepositoryPort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderServicePort {

    private final IOrderRepositoryPort orderRepository;
    private final IEventPublisherPort eventPublisher;

    @Override
    public Mono<Order> updateOrderStatusByEvent(UUID orderId, Consumer<Order> statusUpdater) {
        return getOrderById(orderId)
            .doOnNext(statusUpdater) 
            .flatMap(orderRepository::save);
    }

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
        
        return orderRepository.save(newOrder)
            .flatMap(savedOrder -> {
                
                List<ProductStockDTO> productList = savedOrder.getItems().stream()
                    .map(item -> new ProductStockDTO(item.productId(), item.quantity()))
                    .collect(Collectors.toList());

                OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                    savedOrder.getOrderId(),
                    savedOrder.getClientId(),
                    savedOrder.getTotal(),
                    productList 
                );
                
                // SOLO publicar OrderCreatedEvent y ReserveStockCommand
                // NO publicar ProcessPaymentCommand aquí
                return eventPublisher.publishOrderCreatedEvent(orderCreatedEvent)
                    .then(Mono.defer(() -> {
                        ReserveStockCommand reserveCommand = new ReserveStockCommand(
                            savedOrder.getOrderId(),
                            productList
                        );
                        return eventPublisher.publishReserveStockCommand(reserveCommand)
                            .thenReturn(savedOrder);
                    }));
            });
    }

    // NUEVO MÉTODO para procesar pago
    @Override
    public Mono<Order> processPayment(UUID orderId, String paymentMethod) {
        return getOrderById(orderId)
            .filter(order -> order.getStatus().toString().equals("CREATED"))
            .switchIfEmpty(Mono.error(new IllegalStateException("Solo orders en estado CREATED pueden ser pagadas")))
            .flatMap(order -> {
                // Enviar comando a Payment Service
                ProcessPaymentCommand paymentCommand = new ProcessPaymentCommand(
                    orderId,
                    order.getClientId(),
                    order.getTotal(),
                    paymentMethod
                );
                return eventPublisher.publishProcessPaymentCommand(paymentCommand)
                    .thenReturn(order);
            });
    }

    @Override
    public Mono<Order> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId))); 
    }

    @Override
    public Flux<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Mono<Order> confirmOrder(UUID orderId) {
        return getOrderById(orderId); 
    }

    @Override
    public Mono<Order> shipOrder(UUID orderId) {
        return updateOrderStatusByEvent(orderId, Order::ship); 
    }

    @Override
    public Mono<Order> deliverOrder(UUID orderId) {
        return updateOrderStatusByEvent(orderId, Order::deliver);
    }

    @Override
    public Mono<Order> cancelOrder(UUID orderId) {
        return getOrderById(orderId)
            .doOnNext(Order::cancel) 
            .flatMap(order -> {
                if (order.getStatus().toString().equals("CANCELLED")) {
                    return eventPublisher.publishOrderCancelledEvent(
                        new OrderCancelledEvent(order.getOrderId(), order.getClientId(), "CLIENT_CANCELLED")
                    ).thenReturn(order);
                }
                return Mono.just(order);
            })
            .flatMap(orderRepository::save);
    }
    
    @Override
    public Mono<Order> addProductToOrder(UUID orderId, OrderItem item) {
        return getOrderById(orderId)
            .doOnNext(order -> {
                order.addItem(item);
            })
            .flatMap(orderRepository::save);
    }

    @Override
    public Mono<Order> removeProductFromOrder(UUID orderId, UUID productId) {
        return getOrderById(orderId)
            .doOnNext(order -> {
                order.removeItem(productId);
            })
            .flatMap(orderRepository::save);
    }

    @Override
    public Mono<Order> updateItemQuantity(UUID orderId, UUID productId, int newQuantity) {
        return getOrderById(orderId)
            .doOnNext(order -> {
                order.updateItemQuantity(productId, newQuantity);
            })
            .flatMap(orderRepository::save);
    }
}
