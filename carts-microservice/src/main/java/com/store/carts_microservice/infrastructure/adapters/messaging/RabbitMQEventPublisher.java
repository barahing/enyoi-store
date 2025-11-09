package com.store.carts_microservice.infrastructure.adapters.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import com.store.carts_microservice.domain.ports.out.ICartEventPublisherPort;
import com.store.common.events.CartConvertedEvent;

@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisher implements ICartEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    
    @Value("${app.rabbitmq.exchange:store.events}")
    private String eventsExchangeName;

    @Override
    public Mono<Void> publishCartConverted(CartConvertedEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                System.out.println("🚀🚀🚀 [RABBITMQ DEBUG] START Publishing CartConvertedEvent");
                System.out.println("📦 Event details: " + event);
                System.out.println("🔧 Exchange: " + eventsExchangeName);
                System.out.println("🔑 Routing Key: cart.converted.event");
                System.out.println("🔄 RabbitTemplate: " + rabbitTemplate);
                System.out.println("📊 Connection Factory: " + rabbitTemplate.getConnectionFactory());
                
                // Verificar si el exchange existe
                try {
                    rabbitTemplate.execute(channel -> {
                        System.out.println("✅ Channel created successfully");
                        return null;
                    });
                } catch (Exception e) {
                    System.out.println("❌ Channel creation failed: " + e.getMessage());
                }
                
                rabbitTemplate.convertAndSend(
                    eventsExchangeName,
                    "cart.converted.event",
                    event
                );
                
                System.out.println("✅✅✅ [RABBITMQ DEBUG] SUCCESS: Event published!");
                
            } catch (Exception e) {
                System.out.println("❌❌❌ [RABBITMQ DEBUG] ERROR: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        });
    }
}