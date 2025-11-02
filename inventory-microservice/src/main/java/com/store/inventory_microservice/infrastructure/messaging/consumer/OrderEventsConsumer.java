package com.store.inventory_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.store.common.events.OrderCreatedEvent;
import com.store.common.commands.ReleaseStockCommand;
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
        log.info("Received OrderCreatedEvent for Order ID: {}. Attempting to reserve stock.", event.getOrderId());
        
        productStockService.processOrderCreation(event.getOrderId(), event.getProducts())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                v -> log.info("Stock reservation process finished for Order ID: {}", event.getOrderId()),
                e -> log.error("Error processing stock reservation for Order ID {}: {}", event.getOrderId(), e.getMessage())
            );
    }

    @RabbitListener(queues = "${app.rabbitmq.release-stock-command-queue}")
    public void handleReleaseStockCommand(ReleaseStockCommand command) {
        log.warn("Received ReleaseStockCommand for Order ID: {}. Initiating stock release (Saga Rollback). Reason: {}", 
                 command.getOrderId(), command.getReason());
        
        productStockService.releaseOrderStock(command.getOrderId(), command.getProducts())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                v -> log.info("Stock successfully released for Order ID: {}", command.getOrderId()),
                e -> log.error("Error releasing stock for Order ID {}: {}", command.getOrderId(), e.getMessage())
            );
    }
}
