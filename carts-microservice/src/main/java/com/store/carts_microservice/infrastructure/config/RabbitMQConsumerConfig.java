package com.store.carts_microservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumerConfig {

    // Exchange del servicio de usuarios (debe coincidir con el productor)
    public static final String USER_EXCHANGE_NAME = "user-exchange";
    
    // Cola específica para el microservicio de carritos
    public static final String CART_CREATION_QUEUE_NAME = "cart.creation.queue";
    
    // Routing Key del evento publicado por users-service
    public static final String USER_CREATED_ROUTING_KEY = "user.created.event";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE_NAME);
    }

    @Bean
    public Queue cartCreationQueue() {
        return new Queue(CART_CREATION_QUEUE_NAME, true); 
    }

    @Bean
    public Binding cartCreationBinding(Queue cartCreationQueue, TopicExchange userExchange) {
        return BindingBuilder
                .bind(cartCreationQueue)
                .to(userExchange)
                .with(USER_CREATED_ROUTING_KEY);
    }
}