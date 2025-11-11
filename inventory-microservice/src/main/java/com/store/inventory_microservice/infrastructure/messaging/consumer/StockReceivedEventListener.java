package com.store.inventory_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.store.common.events.StockReceivedEvent;
import com.store.inventory_microservice.application.service.ProductStockService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReceivedEventListener {

    private final ProductStockService inventoryService;

    @RabbitListener(queues = "${app.rabbitmq.stock-received-queue:stock-received-queue}")
    public void handleStockReceivedEvent(StockReceivedEvent event) {
        log.info("📦 [INVENTORY] Received StockReceivedEvent: productId={}, quantity={}, purchaseOrderId={}",
                event.getProductId(), event.getQuantityReceived(), event.getPurchaseOrderId());

        try {
            inventoryService.increaseStock(
                event.getProductId(), 
                event.getQuantityReceived(), 
                event.getPurchaseOrderId()
            ).subscribe(
                updatedStock -> {
                    log.info("✅ [INVENTORY] Stock increased successfully for product: {}. New stock: {}", 
                            event.getProductId(), updatedStock.getCurrentStock());
                },
                error -> {
                    log.error("❌ [INVENTORY] Error processing StockReceivedEvent for product: {}", 
                            event.getProductId(), error);
                }
            );
        } catch (Exception e) {
            log.error("❌ [INVENTORY] Unexpected error processing StockReceivedEvent for product: {}", 
                    event.getProductId(), e);
        }
    }
}