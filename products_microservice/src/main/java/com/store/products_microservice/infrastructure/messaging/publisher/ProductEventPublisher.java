package com.store.products_microservice.infrastructure.messaging.publisher;

import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.store.products_microservice.infrastructure.config.RabbitMQConfig;
import com.store.products_microservice.infrastructure.messaging.events.ProductEvent;

@Component
@RequiredArgsConstructor
public class ProductEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishProductCreated(UUID productId, Integer stock) {
        ProductEvent event = new ProductEvent("PRODUCT_CREATED", productId, stock);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.PRODUCT_EXCHANGE,
            "product.created",
            event
        );
    }

    public void publishStockChanged(UUID productId, Integer stock) {
        ProductEvent event = new ProductEvent("STOCK_CHANGED", productId, stock);
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.PRODUCT_EXCHANGE,
            "product.stock_changed",
            event
        );
    }
}
