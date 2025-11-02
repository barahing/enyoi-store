package com.store.purchases_microservice.infrastructure.config;

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

    @Value("${app.rabbitmq.events-exchange}")
    private String eventsExchangeName;
    
    @Value("${app.rabbitmq.purchases-queue}")
    private String purchasesQueueName;
    
    private static final String LOW_STOCK_ALERT_ROUTING_KEY = "stock.low"; 

    @Bean
    public Exchange eventsExchange() {
        return ExchangeBuilder
                .topicExchange(eventsExchangeName)
                .durable(true)
                .build();
    }

    @Bean
    public Queue purchasesQueue() {
        return new Queue(purchasesQueueName, true);
    }

    @Bean
    public Binding bindingPurchasesQueue() {
        return BindingBuilder
                .bind(purchasesQueue())
                .to(eventsExchange())
                .with(LOW_STOCK_ALERT_ROUTING_KEY)
                .noargs();
    }
}