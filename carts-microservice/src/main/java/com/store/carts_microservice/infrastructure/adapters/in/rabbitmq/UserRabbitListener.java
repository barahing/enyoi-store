package com.store.carts_microservice.infrastructure.adapters.in.rabbitmq;

import com.store.common.events.UserCreatedEvent;
import com.store.common.events.UserDeactivatedEvent;
import com.store.common.events.UserActivatedEvent;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRabbitListener {

    private final ICartServicePort cartServicePort;

    @Value("${app.rabbitmq.user-created-queue:user.created.queue.carts}")
    private String userCreatedQueue;

    @Value("${app.rabbitmq.user-deactivated-queue:user.deactivated.queue}")
    private String userDeactivatedQueue;

    @Value("${app.rabbitmq.user-activated-queue:user.activated.queue}")
    private String userActivatedQueue;

    @RabbitListener(queues = "${app.rabbitmq.user-created-queue:user.created.queue.carts}")
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("👤 [CARTS] Received UserCreatedEvent for client: {}", event.userId());

        try {
            // 💡 Garantiza creación o existencia de carrito antes de ACK
            cartServicePort.getActiveCartByClientId(event.userId())
                .onErrorResume(err -> {
                    // Si no existe, lo creamos
                    log.info("🛒 No active cart found for user {}, creating new one...", event.userId());
                    return cartServicePort.createCartForClient(event.userId());
                })
                .doOnSuccess(cart ->
                    log.info("✅ Cart ready for user {} → {}", event.userId(), cart.getCartId()))
                .doOnError(e ->
                    log.error("❌ Error ensuring cart for user {}: {}", event.userId(), e.getMessage(), e))
                .block(); // 👈 forzamos ejecución antes de ACK
        } catch (Exception e) {
            log.error("❌ [LISTENER] Failed processing UserCreatedEvent for {}: {}", event.userId(), e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.user-deactivated-queue:user.deactivated.queue}")
    public void handleUserDeactivatedEvent(UserDeactivatedEvent event) {
        log.info("🚫 Received UserDeactivatedEvent for client: {}", event.userId());
        cartServicePort.deleteUserCart(event.userId())
            .doOnSuccess(v -> log.info("✅ Deleted cart for deactivated user {}", event.userId()))
            .doOnError(e -> log.error("❌ Error deleting cart for user {}: {}", event.userId(), e.getMessage()))
            .block();
    }

    @RabbitListener(queues = "${app.rabbitmq.user-activated-queue:user.activated.queue}")
    public void handleUserActivatedEvent(UserActivatedEvent event) {
        log.info("🔁 Received UserActivatedEvent for client: {}", event.userId());
        cartServicePort.createCartForClient(event.userId())
            .doOnSuccess(cart -> log.info("✅ Recreated cart {} for reactivated user {}", cart.getCartId(), event.userId()))
            .doOnError(e -> log.error("❌ Error creating cart for reactivated user {}: {}", event.userId(), e.getMessage()))
            .block();
    }
}
