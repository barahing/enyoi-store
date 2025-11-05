package com.store.carts_microservice.infrastructure.adapters.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import com.store.carts_microservice.domain.ports.out.ICartEventPublisherPort;
import com.store.common.events.CartConvertedEvent;
import com.store.carts_microservice.infrastructure.config.RabbitMQProducerConfig;

@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisher implements ICartEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    
    @Override
    public Mono<Void> publishCartConverted(CartConvertedEvent event) {
        return Mono.fromRunnable(() -> {
            rabbitTemplate.convertAndSend(
                RabbitMQProducerConfig.CART_EXCHANGE_NAME,
                RabbitMQProducerConfig.CART_CONVERTED_ROUTING_KEY,
                event
            );
        });
    }
}