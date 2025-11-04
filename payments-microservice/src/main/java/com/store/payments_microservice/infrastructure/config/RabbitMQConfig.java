package com.store.payments_microservice.infrastructure.config;

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

    @Value("${app.rabbitmq.exchange}")
    private String eventsExchangeName;
    
    @Value("${app.rabbitmq.payment-queue}")
    private String paymentQueueName;
    
    private static final String PROCESS_PAYMENT_ROUTING_KEY = "payment.process"; 

    @Bean
    public Exchange eventsExchange() {
        return ExchangeBuilder
                .topicExchange(eventsExchangeName)
                .durable(true)
                .build();
    }

    @Bean
    public Queue paymentQueue() {
        return new Queue(paymentQueueName, true);
    }

    @Bean
    public Binding bindingPaymentQueue() {
        return BindingBuilder
                .bind(paymentQueue())
                .to(eventsExchange())
                 .with(PROCESS_PAYMENT_ROUTING_KEY) 
                .noargs();
    }
}