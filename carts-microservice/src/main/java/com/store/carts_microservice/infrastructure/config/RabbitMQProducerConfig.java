package com.store.carts_microservice.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQProducerConfig {

    public static final String CART_EXCHANGE_NAME = "cart-exchange";
    public static final String CART_CONVERTED_ROUTING_KEY = "cart.converted.event";

    @Bean
    public TopicExchange cartExchange() {
        return new TopicExchange(CART_EXCHANGE_NAME);
    }
}