package com.store.inventory_microservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.inventory-queue}")
    private String inventoryQueueName;
    
    @Value("${app.rabbitmq.exchange}")
    private String eventsExchangeName;

    @Bean
    public Queue inventoryQueue() {
        return new Queue(inventoryQueueName, true); 
    }
    @Bean
    public Exchange eventsExchange() {
        return ExchangeBuilder
                .topicExchange(eventsExchangeName)
                .durable(true)
                .build();
    }
    @Bean
    public Binding bindingInventoryQueueToExchange() {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(eventsExchange())
                .with("#") 
                .noargs();
    }
}