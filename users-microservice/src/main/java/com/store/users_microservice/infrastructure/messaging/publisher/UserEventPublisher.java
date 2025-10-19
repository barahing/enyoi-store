package com.store.users_microservice.infrastructure.messaging.publisher;

import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.store.users_microservice.infrastructure.config.RabbitMQConfig;
import com.store.users_microservice.infrastructure.messaging.events.UserEvent;

@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserCreated(UUID userId) {
        UserEvent event = new UserEvent("USER_CREATED", userId);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.USER_EXCHANGE,
            "user.created",
            event
        );
    }

    public void publishUserDeleted(UUID userId) {
        UserEvent event = new UserEvent("USER_DELETED", userId);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.USER_EXCHANGE,
            "user.deleted",
            event
        );
    }
}
