package com.store.carts_microservice.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String USER_EXCHANGE = "user.events.exchange";
    public static final String PRODUCT_EXCHANGE = "product.events.exchange";

    public static final String USER_QUEUE = "user.events.queue";
    public static final String PRODUCT_QUEUE = "product.events.queue";

    // Users
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userQueue() {
        return new Queue(USER_QUEUE, true);
    }

    @Bean
    public Binding userBinding() {
        return BindingBuilder.bind(userQueue()).to(userExchange()).with("user.*");
    }

    // Products
    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(PRODUCT_EXCHANGE);
    }

    @Bean
    public Queue productQueue() {
        return new Queue(PRODUCT_QUEUE, true);
    }

    @Bean
    public Binding productBinding() {
        return BindingBuilder.bind(productQueue()).to(productExchange()).with("product.*");
    }
}
