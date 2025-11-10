// inventory/infrastructure/adapters/in/rabbitmq/PaymentProcessedEventListener.java
package com.store.inventory_microservice.infrastructure.messaging.consumer;

import com.store.common.events.PaymentProcessedEvent;
import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessedEventListener {

    private final IProductStockServicePort inventoryService;

    @RabbitListener(queues = "${app.rabbitmq.payment-processed-queue}")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("💰 [INVENTORY] ⚡⚡⚡ RECEIVED PaymentProcessedEvent for orderId: {}", event.getOrderId());
        log.info("💰 [INVENTORY] Event details: {}", event.toString()); // Usa toString() en lugar de métodos específicos
        
        // 🔥 VERIFICACIÓN INMEDIATA
        if (event.getOrderId() == null) {
            log.error("💥 [INVENTORY] OrderId is NULL in PaymentProcessedEvent!");
            return;
        }
        
        inventoryService.confirmStockForOrder(event.getOrderId())
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSubscribe(sub -> log.info("🚀 [INVENTORY] Starting stock confirmation for order: {}", event.getOrderId()))
            .subscribe(
                result -> log.info("✅ [INVENTORY] ⚡⚡⚡ Stock confirmed and reservations cleared for orderId: {}", event.getOrderId()),
                error -> {
                    log.error("❌ [INVENTORY] ⚡⚡⚡ Error confirming stock for orderId {}: {}", event.getOrderId(), error.getMessage());
                    error.printStackTrace();
                }
            );
    }
}