package com.store.users_microservice.infrastructure.adapters.out.rabbitmq;

import com.store.common.events.UserCreatedEvent;
import com.store.common.events.UserDeactivatedEvent;
import com.store.common.events.UserActivatedEvent;
import com.store.common.messaging.MessagingConstants;
import com.store.users_microservice.domain.ports.out.IUserEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class RabbitMQUserPublisherAdapter implements IUserEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public Mono<Void> publishUserCreated(UserCreatedEvent event) {
        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(
                MessagingConstants.USER_EXCHANGE,
                MessagingConstants.USER_CREATED_ROUTING_KEY,
                event
            );
        });
    }

    @Override
    public Mono<Void> publishUserDeactivated(UserDeactivatedEvent event) {
        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(
                MessagingConstants.USER_EXCHANGE,
                MessagingConstants.USER_DEACTIVATED_ROUTING_KEY,
                event
            );
        });
    }

    @Override
    public Mono<Void> publishUserActivated(UserActivatedEvent event) {
        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(
                MessagingConstants.USER_EXCHANGE,
                MessagingConstants.USER_ACTIVATED_ROUTING_KEY,
                event
            );
        });
    }
}