package com.store.carts_microservice.infrastructure.adapters.in.rabbitmq;

import com.store.common.events.UserCreatedEvent;
import com.store.common.events.UserDeactivatedEvent;
import com.store.common.events.UserActivatedEvent;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRabbitListener {

    private final ICartServicePort cartServicePort;

    @Value("${app.rabbitmq.user-created-queue:user.created.queue}")
    private String userCreatedQueue;

    @Value("${app.rabbitmq.user-deactivated-queue:user.deactivated.queue}")
    private String userDeactivatedQueue;

    @Value("${app.rabbitmq.user-activated-queue:user.activated.queue}")
    private String userActivatedQueue;

    @RabbitListener(queues = "${app.rabbitmq.user-created-queue:user.created.queue}")
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("👤 Received UserCreatedEvent for client: {}", event.userId());

        cartServicePort.createCartForClient(event.userId())
            .subscribe(
                cart -> log.info("✅ Created cart {} for user {}", cart.getCartId(), event.userId()),
                error -> log.error("❌ Error creating cart for user {}: {}", event.userId(), error.getMessage())
            );
    }

    @RabbitListener(queues = "${app.rabbitmq.user-deactivated-queue:user.deactivated.queue}")
    public void handleUserDeactivatedEvent(UserDeactivatedEvent event) {
        log.info("🚫 Received UserDeactivatedEvent for client: {}", event.userId());

        cartServicePort.deleteUserCart(event.userId())
            .subscribe(
                result -> log.info("✅ Deleted cart for deactivated user {}", event.userId()),
                error -> log.error("❌ Error deleting cart for user {}: {}", event.userId(), error.getMessage())
            );
    }

    @RabbitListener(queues = "${app.rabbitmq.user-activated-queue:user.activated.queue}")
    public void handleUserActivatedEvent(UserActivatedEvent event) {
        log.info("🔁 Received UserActivatedEvent for client: {}", event.userId());

        cartServicePort.createCartForClient(event.userId())
            .subscribe(
                cart -> log.info("✅ Recreated cart {} for reactivated user {}", cart.getCartId(), event.userId()),
                error -> log.error("❌ Error creating cart for reactivated user {}: {}", event.userId(), error.getMessage())
            );
    }
}
