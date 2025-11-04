package com.store.orders_microservice.infrastructure.config;

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
    
    @Value("${app.rabbitmq.orders-saga-queue}")
    private String ordersSagaQueueName;

    private static final String PAYMENT_PROCESSED_KEY = "payment.processed"; 
    private static final String PAYMENT_FAILED_KEY = "payment.failed"; 
    private static final String STOCK_RESERVED_KEY = "stock.reserved"; 
    private static final String STOCK_RESERVATION_FAILED_KEY = "stock.failed"; 

    @Bean
    public Exchange eventsExchange() {
        return ExchangeBuilder
                .topicExchange(eventsExchangeName)
                .durable(true)
                .build();
    }

    @Bean
    public Queue ordersSagaQueue() {
        return new Queue(ordersSagaQueueName, true);
    }

    @Bean
    public Binding bindingPaymentProcessed() {
        return BindingBuilder
                .bind(ordersSagaQueue())
                .to(eventsExchange())
                .with(PAYMENT_PROCESSED_KEY)
                .noargs();
    }
    
    @Bean
    public Binding bindingPaymentFailed() {
        return BindingBuilder
                .bind(ordersSagaQueue())
                .to(eventsExchange())
                .with(PAYMENT_FAILED_KEY)
                .noargs();
    }
    
    @Bean
    public Binding bindingStockReserved() {
        return BindingBuilder
                .bind(ordersSagaQueue())
                .to(eventsExchange())
                .with(STOCK_RESERVED_KEY)
                .noargs();
    }
    
    @Bean
    public Binding bindingStockReservationFailed() {
        return BindingBuilder
                .bind(ordersSagaQueue())
                .to(eventsExchange())
                .with(STOCK_RESERVATION_FAILED_KEY)
                .noargs();
    }
}