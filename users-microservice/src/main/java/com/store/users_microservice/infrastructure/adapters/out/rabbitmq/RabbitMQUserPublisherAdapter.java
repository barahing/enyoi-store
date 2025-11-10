package com.store.users_microservice.infrastructure.adapters.out.rabbitmq;

import com.store.common.events.UserCreatedEvent;
import com.store.common.events.UserDeactivatedEvent;
import com.store.common.events.UserActivatedEvent;
import com.store.common.messaging.MessagingConstants;
import com.store.users_microservice.domain.ports.out.IUserEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQUserPublisherAdapter implements IUserEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public Mono<Void> publishUserCreated(UserCreatedEvent event) {
        // 🔎 LOG antes de publicar
        log.info("👤 [USERS] Publishing UserCreatedEvent -> exchange='{}' key='{}' payload={}",
                MessagingConstants.USER_EXCHANGE, MessagingConstants.USER_CREATED_ROUTING_KEY, event);

        rabbitTemplate.convertAndSend(
            MessagingConstants.USER_EXCHANGE,
            MessagingConstants.USER_CREATED_ROUTING_KEY,
            event
        );

        // 🔎 LOG después de invocar convertAndSend (no implica confirmación; para eso están los callbacks de abajo)
        log.info("👤 [USERS] convertAndSend invoked (UserCreatedEvent)");
        return Mono.empty();
    }

    @Override
    public Mono<Void> publishUserDeactivated(UserDeactivatedEvent event) {
        log.info("👤 [USERS] Publishing UserDeactivatedEvent -> exchange='{}' key='{}'",
                MessagingConstants.USER_EXCHANGE, MessagingConstants.USER_DEACTIVATED_ROUTING_KEY);
        return Mono.fromRunnable(() -> rabbitTemplate.convertAndSend(
                MessagingConstants.USER_EXCHANGE,
                MessagingConstants.USER_DEACTIVATED_ROUTING_KEY,
                event
        ));
    }

    @Override
    public Mono<Void> publishUserActivated(UserActivatedEvent event) {
        log.info("👤 [USERS] Publishing UserActivatedEvent -> exchange='{}' key='{}'",
                MessagingConstants.USER_EXCHANGE, MessagingConstants.USER_ACTIVATED_ROUTING_KEY);
        return Mono.fromRunnable(() -> rabbitTemplate.convertAndSend(
                MessagingConstants.USER_EXCHANGE,
                MessagingConstants.USER_ACTIVATED_ROUTING_KEY,
                event
        ));
    }
}
