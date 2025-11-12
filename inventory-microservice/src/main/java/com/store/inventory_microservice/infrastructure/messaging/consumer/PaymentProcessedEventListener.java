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

    @RabbitListener(
        id = "inventoryPaymentProcessedListener",
        queues = "${app.rabbitmq.payment-processed-queue}",
        concurrency = "1" 
    )
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("💰 [INVENTORY] RECEIVED PaymentProcessedEvent | orderId={}", event.getOrderId());

        if (event.getOrderId() == null) {
            log.error("💥 [INVENTORY] OrderId is NULL in PaymentProcessedEvent!");
            return;
        }

        inventoryService.confirmStockForOrder(event.getOrderId())
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSubscribe(sub -> log.info("🚀 [INVENTORY] Confirming stock for order: {}", event.getOrderId()))
            .subscribe(
                result -> log.info("✅ [INVENTORY] Stock confirmed for orderId: {}", event.getOrderId()),
                error -> log.error("❌ [INVENTORY] Error confirming stock for orderId {}: {}", event.getOrderId(), error.getMessage(), error)
            );
    }
}
