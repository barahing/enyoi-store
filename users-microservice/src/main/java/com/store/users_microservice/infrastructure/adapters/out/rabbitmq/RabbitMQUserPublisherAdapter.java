package com.store.users_microservice.infrastructure.adapters.out.rabbitmq;

import com.store.common.events.UserCreatedEvent;
import com.store.users_microservice.domain.ports.out.IUserEventPublisherPort;
import com.store.users_microservice.infrastructure.config.RabbitMQProducerConfig;
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
                RabbitMQProducerConfig.USER_EXCHANGE_NAME, 
                "user.created.event", 
                event
            );
        });
    }
}