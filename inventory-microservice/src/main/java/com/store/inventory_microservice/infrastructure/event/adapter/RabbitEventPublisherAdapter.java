package com.store.inventory_microservice.infrastructure.event.adapter;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.store.common.events.LowStockAlertEvent;
import com.store.common.events.StockReservationFailedEvent;
import com.store.common.events.StockReservedEvent;
import com.store.inventory_microservice.domain.ports.out.IEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitEventPublisherAdapter implements IEventPublisherPort {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${app.rabbitmq.exchange}") 
    private String eventsExchange;

    private static final String KEY_STOCK_RESERVED = "stock.reserved";
    private static final String KEY_STOCK_RESERVATION_FAILED = "stock.failed";
    private static final String LOW_STOCK_ALERT_ROUTING_KEY = "inventory.lowstock.alert";


    private Mono<Void> sendEvent(String routingKey, Object event) {
        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(eventsExchange, routingKey, event);
            log.info("Event sent: {} with Routing Key: {}", event.getClass().getSimpleName(), routingKey);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishStockReservedEvent(StockReservedEvent event) {
        return sendEvent(KEY_STOCK_RESERVED, event);
    }
    
    @Override
    public Mono<Void> publishStockReservationFailedEvent(StockReservationFailedEvent event) {
        return sendEvent(KEY_STOCK_RESERVATION_FAILED, event);
    }

     @Override
    public Mono<Void> publishLowStockAlertEvent(LowStockAlertEvent event) {
        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(eventsExchange, LOW_STOCK_ALERT_ROUTING_KEY, event);
            log.info("📤 [RABBIT] Sent LowStockAlertEvent to {} with {} products below threshold {}",
                    LOW_STOCK_ALERT_ROUTING_KEY,
                    event.getCurrentStock(),
                    event.getReorderLevel());
        });
    }
}