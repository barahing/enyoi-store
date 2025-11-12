package com.store.orders_microservice.infrastructure.messaging.publisher;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.store.common.commands.ProcessPaymentCommand; 
import com.store.common.commands.ReleaseStockCommand; 
import com.store.common.commands.ReserveStockCommand; 
import com.store.common.events.OrderCancelledEvent;
import com.store.common.events.OrderConfirmedEvent;
import com.store.common.events.OrderCreatedEvent;
import com.store.common.events.OrderDeliveredEvent;
import com.store.common.events.OrderShippedEvent;
import com.store.orders_microservice.domain.ports.out.IEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitEventPublisherAdapter implements IEventPublisherPort {

    private final AmqpTemplate amqpTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String eventsExchange;

    private static final String KEY_ORDER_CREATED = "order.created";
    private static final String KEY_ORDER_CONFIRMED = "order.confirmed";
    private static final String KEY_ORDER_CANCELLED = "order.cancelled";
    private static final String KEY_PROCESS_PAYMENT = "payment.process"; 
    private static final String KEY_RESERVE_STOCK = "stock.reserve"; 
    private static final String KEY_RELEASE_STOCK = "stock.release"; 
    private static final String KEY_ORDER_SHIPPED = "order.shipped";
    private static final String KEY_ORDER_DELIVERED = "order.delivered";

    private Mono<Void> sendEvent(String routingKey, Object event) {
        return Mono.fromRunnable(() -> {
            amqpTemplate.convertAndSend(eventsExchange, routingKey, event);
            log.info("Event sent: {} with Routing Key: {}", event.getClass().getSimpleName(), routingKey);
        }).then();
    }

    @Override
    public Mono<Void> publishOrderCreatedEvent(OrderCreatedEvent event) {
        return sendEvent(KEY_ORDER_CREATED, event);
    }

    @Override
    public Mono<Void> publishOrderConfirmedEvent(OrderConfirmedEvent event) {
        return sendEvent(KEY_ORDER_CONFIRMED, event);
    }

    @Override
    public Mono<Void> publishOrderCancelledEvent(OrderCancelledEvent event) {
        return sendEvent(KEY_ORDER_CANCELLED, event);
    }

    @Override
    public Mono<Void> publishReleaseStockCommand(ReleaseStockCommand command) {
        return sendEvent(KEY_RELEASE_STOCK, command);
    }
    
    @Override
    public Mono<Void> publishProcessPaymentCommand(ProcessPaymentCommand command) {
        return sendEvent(KEY_PROCESS_PAYMENT, command);
    }

    @Override
    public Mono<Void> publishReserveStockCommand(ReserveStockCommand command) {
        return sendEvent(KEY_RESERVE_STOCK, command);
    }

    @Override
    public Mono<Void> publishOrderShippedEvent(OrderShippedEvent event) {
        return sendEvent(KEY_ORDER_SHIPPED, event);
    }

    @Override
    public Mono<Void> publishOrderDeliveredEvent(OrderDeliveredEvent event) {
        return sendEvent(KEY_ORDER_DELIVERED, event);
    }
}