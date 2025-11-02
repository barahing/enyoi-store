package com.store.notifications_microservice.infrastructure.config;

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
    
    @Value("${app.rabbitmq.notification-queue}")
    private String notificationQueueName;
    
    private static final String ORDER_CONFIRMED_KEY = "order.confirmed"; 
    private static final String PAYMENT_FAILED_KEY = "payment.failed"; 

    @Bean
    public Exchange eventsExchange() {
        return ExchangeBuilder
                .topicExchange(eventsExchangeName)
                .durable(true)
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(notificationQueueName, true);
    }

    @Bean
    public Binding bindingOrderConfirmed() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(eventsExchange())
                .with(ORDER_CONFIRMED_KEY)
                .noargs();
    }
    
    @Bean
    public Binding bindingPaymentFailed() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(eventsExchange())
                .with(PAYMENT_FAILED_KEY)
                .noargs();
    }
}