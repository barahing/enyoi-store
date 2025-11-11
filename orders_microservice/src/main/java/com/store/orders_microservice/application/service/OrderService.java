package com.store.orders_microservice.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.store.common.commands.ProcessPaymentCommand;
import com.store.common.commands.ReserveStockCommand;
import com.store.common.dto.ProductStockDTO;
import com.store.common.events.CartConvertedEvent;
import com.store.common.events.OrderCancelledEvent;
import com.store.common.events.OrderCreatedEvent;
import com.store.common.events.OrderDeliveredEvent;
import com.store.common.events.OrderShippedEvent;
import com.store.orders_microservice.domain.exception.OrderNotFoundException;
import com.store.orders_microservice.domain.factory.OrderFactory;
import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.model.OrderItem;
import com.store.orders_microservice.domain.model.OrderStatus;
import com.store.orders_microservice.domain.ports.in.IOrderServicePort;
import com.store.orders_microservice.domain.ports.out.IEventPublisherPort;
import com.store.orders_microservice.domain.ports.out.IOrderRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
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

                // Publicar OrderCreatedEvent y ReserveStockCommand
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

    @Override
    public Mono<Order> processPayment(UUID orderId, String paymentMethod) {
        return getOrderById(orderId)
            .flatMap(order -> {
                if (order.getStatus() != OrderStatus.STOCK_RESERVED) {
                    return Mono.error(new IllegalStateException(
                        "Solo orders en estado STOCK_RESERVED pueden ser pagadas. Estado actual: " +
                        order.getStatus()));
                }

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

    /**
     * 🚚 Cuando la orden se marca como enviada (SHIPPED),
     * se publica el evento OrderShippedEvent.
     */
    @Override
    public Mono<Order> shipOrder(UUID orderId) {
        return updateOrderStatusByEvent(orderId, Order::ship)
            .flatMap(order ->
                eventPublisher.publishOrderShippedEvent(
                    new OrderShippedEvent(
                        order.getOrderId(),
                        order.getClientId(),
                        "TRACK-" + order.getOrderId().toString().substring(0, 8)
                    )
                ).thenReturn(order)
            )
            .doOnSuccess(order -> log.info("📦 Order {} marked as SHIPPED", order.getOrderId()));
    }

    /**
     * 📬 Cuando la orden se marca como entregada (DELIVERED),
     * se publica el evento OrderDeliveredEvent.
     */
    @Override
    public Mono<Order> deliverOrder(UUID orderId) {
        return updateOrderStatusByEvent(orderId, Order::deliver)
            .flatMap(order ->
                eventPublisher.publishOrderDeliveredEvent(
                    new OrderDeliveredEvent(
                        order.getOrderId(),
                        order.getClientId(),
                        LocalDateTime.now().toString()
                    )
                ).thenReturn(order)
            )
            .doOnSuccess(order -> log.info("📬 Order {} marked as DELIVERED", order.getOrderId()));
    }

    @Override
    public Mono<Order> cancelOrder(UUID orderId) {
        return getOrderById(orderId)
            .flatMap(order -> {
                try {
                    order.cancel(); // ahora dentro del flujo reactivo
                    return orderRepository.save(order)
                        .flatMap(savedOrder -> eventPublisher.publishOrderCancelledEvent(
                            new OrderCancelledEvent(
                                savedOrder.getOrderId(),
                                savedOrder.getClientId(),
                                savedOrder.getCancellationReason() != null
                                    ? savedOrder.getCancellationReason()
                                    : "CLIENT_CANCELLED"
                            )
                        ).thenReturn(savedOrder));
                } catch (Exception e) {
                    return Mono.error(e); // manejable dentro del flujo Reactor
                }
            })
            .doOnSuccess(order -> log.info("🚨 Order {} cancelled successfully", order.getOrderId()))
            .doOnError(e -> log.error("❌ Error cancelling order {}: {}", orderId, e.getMessage()));
    }

    @Override
    public Mono<Order> addProductToOrder(UUID orderId, OrderItem item) {
        return getOrderById(orderId)
            .doOnNext(order -> order.addItem(item))
            .flatMap(orderRepository::save);
    }

    @Override
    public Mono<Order> removeProductFromOrder(UUID orderId, UUID productId) {
        return getOrderById(orderId)
            .doOnNext(order -> order.removeItem(productId))
            .flatMap(orderRepository::save);
    }

    @Override
    public Mono<Order> updateItemQuantity(UUID orderId, UUID productId, int newQuantity) {
        return getOrderById(orderId)
            .doOnNext(order -> order.updateItemQuantity(productId, newQuantity))
            .flatMap(orderRepository::save);
    }
}
