package com.store.carts_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.store.carts_microservice.infrastructure.config.RabbitMQConfig;
import com.store.carts_microservice.infrastructure.messaging.cache.CartProductCache;
import com.store.products_microservice.infrastructure.messaging.events.ProductEvent; // ⚠️ usar mismo paquete

@Component
@RequiredArgsConstructor
public class ProductEventConsumer {

    private final CartProductCache cache;

    @RabbitListener(queues = RabbitMQConfig.PRODUCT_QUEUE)
    public void on(ProductEvent event) {
        switch (event.getType()) {
            case "PRODUCT_CREATED" -> cache.put(event.getProductId(), event.getStock());
            case "STOCK_CHANGED" -> cache.put(event.getProductId(), event.getStock());
        }
    }
}
