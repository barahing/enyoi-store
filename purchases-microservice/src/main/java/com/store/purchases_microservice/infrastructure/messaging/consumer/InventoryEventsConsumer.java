package com.store.purchases_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.store.purchases_microservice.domain.ports.in.IPurchaseServicePorts;
import com.store.common.events.LowStockAlertEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventsConsumer {

    private final IPurchaseServicePorts purchaseServicePorts;

    @RabbitListener(queues = "${app.rabbitmq.purchases-queue}")
    public void handleLowStockAlert(LowStockAlertEvent event) {
        log.warn("Received LowStockAlert for Product ID: {}. Current stock: {}", 
            event.getProductId(), event.getCurrentStock());

        int quantityToOrder = event.getReorderLevel() * 2;
        
        purchaseServicePorts.createPurchaseOrder(
                "Simulated Supplier Inc.", 
                event.getProductId(), 
                quantityToOrder, 
                java.math.BigDecimal.valueOf(10.00))
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSuccess(order -> log.info("Successfully created Purchase Order {} to replenish stock for Product ID: {}", order.getId(), event.getProductId()))
            .doOnError(e -> log.error("Error creating purchase order for Product ID {}: {}", event.getProductId(), e.getMessage()))
            .subscribe();
    }
}