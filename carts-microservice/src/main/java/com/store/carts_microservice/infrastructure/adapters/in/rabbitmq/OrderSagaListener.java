package com.store.carts_microservice.infrastructure.adapters.in.rabbitmq;

import com.store.carts_microservice.domain.model.CartStatus;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;
import com.store.common.events.OrderCreatedEvent;
import com.store.common.events.OrderConfirmedEvent;
import com.store.common.events.StockReservedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSagaListener {

    private final ICartServicePort cartServicePort;

    /**
     * 1️⃣ OrderCreatedEvent → Linkear orderId al cart activo y ponerlo en estado CONVERTING.
     */
    @RabbitListener(queues = "${app.rabbitmq.order-created-queue}")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("🟢 [CARTS] Received OrderCreatedEvent for orderId={} clientId={}", event.getOrderId(), event.getUserId());

        cartServicePort.linkOrderToCart(event.getUserId(), event.getOrderId())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                v -> log.info("✅ Linked cart to orderId={} (status=CONVERTING)", event.getOrderId()),
                e -> log.error("❌ Failed linking cart to orderId {}: {}", event.getOrderId(), e.getMessage())
            );
    }

    /**
     * 2️⃣ OrderConfirmedEvent → marcar cart como CONVERTED_TO_ORDER y crear nuevo cart vacío.
     */
    @RabbitListener(queues = "${app.rabbitmq.order-confirmed-queue}")
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("📦 [CARTS] Received OrderConfirmedEvent for orderId={} userId={}", event.getOrderId(), event.getUserId());

        cartServicePort.updateCartStatusByOrderId(event.getOrderId(), CartStatus.CONVERTED_TO_ORDER)
            .then(cartServicePort.createCartForClient(event.getUserId()))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                newCart -> log.info("✅ Old cart converted, new ACTIVE cart {} created for user {}", newCart.getCartId(), event.getUserId()),
                e -> log.error("❌ Error updating/creating cart for user {}: {}", event.getUserId(), e.getMessage())
            );
    }

    /**
     * 3️⃣ StockReservedEvent → solo registrar, no eliminar.
     * (El cart se elimina o cierra al confirmar la orden).
     */
    @RabbitListener(queues = "${app.rabbitmq.stock-reserved-queue}")
    public void handleStockReservedEvent(StockReservedEvent event) {
        log.info("📦 [CARTS] Received StockReservedEvent for orderId={}. No action (cart managed by OrderConfirmedEvent)", event.orderId());
    }
}
