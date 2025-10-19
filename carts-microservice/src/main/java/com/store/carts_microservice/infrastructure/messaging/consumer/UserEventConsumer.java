package com.store.carts_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.store.carts_microservice.infrastructure.config.RabbitMQConfig;
import com.store.carts_microservice.infrastructure.messaging.cache.CartUserCache;
import com.store.users_microservice.infrastructure.messaging.events.UserEvent; // ⚠️ usar mismo paquete

@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final CartUserCache cache;

    @RabbitListener(queues = RabbitMQConfig.USER_QUEUE)
    public void on(UserEvent event) {
        switch (event.getType()) {
            case "USER_CREATED" -> cache.add(event.getUserId());
            case "USER_DELETED" -> cache.remove(event.getUserId());
        }
    }
}
