package com.store.carts_microservice.infrastructure.adapters.in.rabbitmq;

import com.store.carts_microservice.domain.model.CartStatus;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;
import com.store.common.events.OrderCreatedEvent;
import com.store.common.events.OrderConfirmedEvent;
import com.store.common.events.StockReservedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
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

    @RabbitListener(queues = "${app.rabbitmq.order-confirmed-queue}")
    public void handleOrderConfirmedEvent(OrderConfirmedEvent event) {
        log.info("📦 [CARTS] Received OrderConfirmedEvent for orderId={} userId={}. No action needed (cart already handled by StockReservedEvent)", 
                event.getOrderId(), event.getUserId());
        // Solo log, no acción - el carrito ya fue manejado por StockReservedEvent
    }

    @RabbitListener(queues = "${app.rabbitmq.stock-reserved-queue}")
    public void handleStockReservedEvent(StockReservedEvent event) {
        log.info("📦 [CARTS] Received StockReservedEvent for orderId={}", event.orderId());

        Mono<UUID> orderIdMono = Mono.just(event.orderId());
        
        orderIdMono.flatMap(orderId -> 
            cartServicePort.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new IllegalStateException("No cart found for orderId: " + orderId)))
                .flatMap(cart -> {
                    UUID clientId = cart.getClientId();
                    return cartServicePort.updateCartStatusByOrderId(orderId, CartStatus.CONVERTED_TO_ORDER)
                        .then(cartServicePort.createCartForClient(clientId));
                })
        )
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe(
            newCart -> log.info("✅ Cart converted and new active cart created: {}", newCart.getCartId()),
            e -> log.error("❌ Error converting cart for orderId {}: {}", event.orderId(), e.getMessage())
        );
    }
}
