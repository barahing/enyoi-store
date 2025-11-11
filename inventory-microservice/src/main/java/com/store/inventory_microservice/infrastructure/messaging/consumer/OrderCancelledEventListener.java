package com.store.inventory_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.store.common.events.OrderCancelledEvent;
import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledEventListener {

    private final IProductStockServicePort stockService;

    @RabbitListener(queues = "${app.rabbitmq.order-cancelled-queue:order-cancelled-queue}")
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        log.warn("🚨 [INVENTORY] Received OrderCancelledEvent for orderId={}, reason={}", 
                 event.getOrderId(), event.getReason());

        stockService.releaseOrderStock(event.getOrderId(), null)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSubscribe(sub -> log.info("🔄 [INVENTORY] Starting stock release for order {}", event.getOrderId()))
            .doOnSuccess(v -> log.info("✅ [INVENTORY] Stock released successfully for cancelled order {}", event.getOrderId()))
            .doOnError(e -> log.error("❌ [INVENTORY] Error releasing stock for cancelled order {}: {}", event.getOrderId(), e.getMessage()))
            .subscribe();
    }
}
