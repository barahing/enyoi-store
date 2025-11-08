package com.store.carts_microservice.infrastructure.adapters.in.rabbitmq;

import com.store.common.events.UserCreatedEvent;
import com.store.common.events.UserDeactivatedEvent;
import com.store.common.events.UserActivatedEvent;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;
import com.store.carts_microservice.infrastructure.config.RabbitMQConsumerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRabbitListener {

    private final ICartServicePort cartServicePort;

    @RabbitListener(queues = RabbitMQConsumerConfig.CART_CREATION_QUEUE_NAME)
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent for client: {}", event.userId());
        
        cartServicePort.createCartForClient(event.userId())
            .subscribe(
                cart -> log.info("Successfully created cart {} for client {}", cart.getCartId(), event.userId()),
                error -> log.error("Error creating cart for client {}: {}", event.userId(), error.getMessage())
            );
    }

    @RabbitListener(queues = RabbitMQConsumerConfig.USER_DEACTIVATED_QUEUE_NAME)
    public void handleUserDeactivatedEvent(UserDeactivatedEvent event) {
        log.info("Received UserDeactivatedEvent for client: {}", event.userId());
        
        cartServicePort.deleteUserCart(event.userId())
            .subscribe(
                result -> log.info("Successfully deleted cart for deactivated client: {}", event.userId()),
                error -> log.error("Error deleting cart for deactivated client {}: {}", event.userId(), error.getMessage())
            );
    }

    @RabbitListener(queues = RabbitMQConsumerConfig.USER_ACTIVATED_QUEUE_NAME)
    public void handleUserActivatedEvent(UserActivatedEvent event) {
        log.info("Received UserActivatedEvent for client: {}", event.userId());
        
        cartServicePort.createCartForClient(event.userId())
            .subscribe(
                cart -> log.info("Successfully created cart {} for reactivated client {}", cart.getCartId(), event.userId()),
                error -> log.error("Error creating cart for reactivated client {}: {}", event.userId(), error.getMessage())
            );
    }
}