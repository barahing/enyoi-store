package com.store.inventory_microservice.infrastructure.event.adapter;

import java.util.List;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;
import com.store.inventory_microservice.infrastructure.event.dto.OrderConfirmedEvent;
import com.store.inventory_microservice.infrastructure.event.dto.OrderItemEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventsConsumer {

    private final IProductStockServicePort stockServicePort;
    private final OrderEventPublisher eventPublisher;

    @RabbitListener(queues = "${app.rabbitmq.inventory-queue}")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent for Order ID: {}", event.getOrderId());

        List<Mono<Void>> reservationMonos = event.getItems().stream()
            .map(item -> reserveStockForItem(event.getOrderId(), item))
            .toList();

        Mono.when(reservationMonos)
            .subscribeOn(Schedulers.boundedElastic()) 
            .doOnSuccess(v -> {
                log.info("Successfully reserved stock for all items in Order {}", event.getOrderId());
                eventPublisher.publishStockReserved(event.getOrderId()); 
            })
            .doOnError(e -> {
                log.error("Failed to reserve stock for Order {}: {}", event.getOrderId(), e.getMessage());
            })
            .subscribe();
    }

    private Mono<Void> reserveStockForItem(UUID orderId, OrderItemEventDto item) {
        return stockServicePort.reserveStock(item.getProductId(), item.getQuantity())
            .onErrorResume(e -> {
                String reason = String.format("Not enough stock: %s", e.getMessage());
                log.warn(reason);
                return Mono.fromRunnable(() -> eventPublisher.publishStockReservationFailed(orderId, reason))
                           .then(Mono.error(new RuntimeException(reason)));
            })
            .then();
    }
}