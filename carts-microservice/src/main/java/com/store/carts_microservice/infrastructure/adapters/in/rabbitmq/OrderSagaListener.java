package com.store.carts_microservice.infrastructure.adapters.in.rabbitmq;

import com.store.carts_microservice.domain.model.CartStatus;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;
import com.store.common.events.OrderCreatedEvent;
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

    @RabbitListener(queues = "${app.rabbitmq.stock-reserved-queue}")
    public void handleStockReservedEvent(StockReservedEvent event) {
        log.info("📦 [CARTS] Received StockReservedEvent for orderId={}", event.orderId());

        Mono.defer(() ->
            cartServicePort.findByOrderId(event.orderId())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("⚠️ No cart found yet for orderId {} — will retry", event.orderId());
                    return Mono.error(new IllegalStateException("Cart not yet linked to order"));
                }))
                .flatMap(cart -> {
                    UUID clientId = cart.getClientId();
                    return cartServicePort.updateCartStatusByOrderId(event.orderId(), CartStatus.CONVERTED_TO_ORDER)
                        .then(cartServicePort.createCartForClient(clientId));
                })
        )
        .retryWhen(reactor.util.retry.Retry.fixedDelay(5, java.time.Duration.ofSeconds(1))
            .filter(e -> e instanceof IllegalStateException)
            .onRetryExhaustedThrow((spec, signal) ->
                new RuntimeException("❌ Cart not linked after retries for orderId " + event.orderId(), signal.failure()))
        )
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe(
            newCart -> log.info("✅ Cart converted and new active cart created: {}", newCart.getCartId()),
            e -> log.error("❌ Error converting cart for orderId {}: {}", event.orderId(), e.getMessage())
        );
    }

}
