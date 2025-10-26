package com.store.orders_microservice.infrastructure.adapters.in.rabbitmq;

import com.store.common.events.CartConvertedEvent;
import com.store.orders_microservice.domain.ports.in.IOrderServicePort;
import com.store.orders_microservice.infrastructure.config.RabbitMQConsumerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartConvertedRabbitListener {

    private final IOrderServicePort orderServicePort;

    @RabbitListener(queues = RabbitMQConsumerConfig.ORDER_CREATION_QUEUE_NAME)
    public void handleCartConvertedEvent(CartConvertedEvent event) {
        log.info("Received CartConvertedEvent for order creation. Client: {}", event.clientId());
        
        orderServicePort.createOrderFromCart(event)
            .subscribe(
                order -> log.info("Successfully created Order {} for client {}", order.getOrderId(), event.clientId()),
                error -> log.error("Error creating order for client {}: {}", event.clientId(), error.getMessage())
            );
    }
}