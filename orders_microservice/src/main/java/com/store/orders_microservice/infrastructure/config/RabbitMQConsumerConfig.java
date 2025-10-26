package com.store.orders_microservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumerConfig {

    public static final String ORDER_EXCHANGE_NAME = "order-exchange";
    
    public static final String ORDER_CREATION_QUEUE_NAME = "order.creation.queue";
    
    public static final String CART_CONVERTED_ROUTING_KEY = "cart.converted.event";

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE_NAME);
    }

    @Bean
    public Queue orderCreationQueue() {
        return new Queue(ORDER_CREATION_QUEUE_NAME, true); 
    }

    @Bean
    public Binding orderCreationBinding(Queue orderCreationQueue, TopicExchange orderExchange) {
        return BindingBuilder
                .bind(orderCreationQueue)
                .to(orderExchange)
                .with(CART_CONVERTED_ROUTING_KEY);
    }
}