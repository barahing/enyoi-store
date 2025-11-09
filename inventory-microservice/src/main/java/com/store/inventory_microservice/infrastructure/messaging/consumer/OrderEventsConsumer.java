package com.store.inventory_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.store.common.commands.ReleaseStockCommand;
import com.store.common.commands.ReserveStockCommand;
import com.store.common.events.OrderCreatedEvent;
import com.store.common.events.PaymentProcessedEvent;
import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventsConsumer {

    private final IProductStockServicePort productStockService;

    // 1️⃣ Evento: se crea la orden → inicia el proceso normal de reserva
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

    // 2️⃣ Evento: se procesa el pago → confirmar reservas
    @RabbitListener(queues = "${app.rabbitmq.payment-processed-queue}")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        log.info("💰 Received PaymentProcessedEvent for Order ID: {}. Confirming stock reservation.", event.getOrderId());

        productStockService.confirmStockReservation(event.getOrderId())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                v -> log.info("✅ Stock reservation confirmed successfully for Order ID: {}", event.getOrderId()),
                e -> log.error("❌ Error confirming stock reservation for Order ID {}: {}", event.getOrderId(), e.getMessage())
            );
    }

    // 4️⃣ Comando: reintento manual o idempotente (no doble ejecución)
    @RabbitListener(queues = "${app.rabbitmq.reserve-stock-command-queue}")
    public void handleReserveStockCommand(ReserveStockCommand command) {
        log.info("⚙️ Received ReserveStockCommand for Order ID: {}.", command.orderId());

        // Antes de ejecutar, validar si ya existen reservas
        productStockService.isQuantityAvailable(command.items().get(0).productId(), command.items().get(0).quantity())
            .flatMap(avail -> {
                if (!avail) {
                    log.warn("♻️ Order {} already processed or insufficient stock, skipping duplicate ReserveStockCommand.", command.orderId());
                    return reactor.core.publisher.Mono.empty();
                }
                // En un escenario real podrías reintentar aquí, pero no automático.
                log.info("ℹ️ ReserveStockCommand received for Order {} — no action taken (handled by OrderCreatedEvent).", command.orderId());
                return reactor.core.publisher.Mono.empty();
            })
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }
}
