package com.store.inventory_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.store.common.commands.ReserveStockCommand;
import com.store.common.events.OrderCreatedEvent;
import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventsConsumer {

    private final IProductStockServicePort productStockService;

    @RabbitListener(queues = "${app.rabbitmq.order-created-queue}")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("📦 Received OrderCreatedEvent for Order ID: {}. Attempting to reserve stock.", event.getOrderId());

        productStockService.processOrderCreation(event.getOrderId(), event.getProducts())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                v -> log.info("✅ Stock reservation process finished for Order ID: {}", event.getOrderId()),
                e -> log.error("❌ Error processing stock reservation for Order ID {}: {}", event.getOrderId(), e.getMessage())
            );
    }

    @RabbitListener(queues = "${app.rabbitmq.reserve-stock-command-queue}")
    public void handleReserveStockCommand(ReserveStockCommand command) {
        log.info("⚙️ Received ReserveStockCommand for Order ID: {}.", command.orderId());

        productStockService.isQuantityAvailable(command.items().get(0).productId(), command.items().get(0).quantity())
            .flatMap(avail -> {
                if (!avail) {
                    log.warn("♻️ Order {} already processed or insufficient stock, skipping duplicate ReserveStockCommand.", command.orderId());
                    return reactor.core.publisher.Mono.empty();
                }
                log.info("ℹ️ ReserveStockCommand received for Order {} — no action taken (handled by OrderCreatedEvent).", command.orderId());
                return reactor.core.publisher.Mono.empty();
            })
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }
}
