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
    
    // Usar las queues específicas en lugar de una sola
    @Value("${app.rabbitmq.payment-processed-queue}")
    private String paymentProcessedQueue;

    @Value("${app.rabbitmq.payment-failed-queue}")
    private String paymentFailedQueue;

    @Value("${app.rabbitmq.stock-reserved-queue}")
    private String stockReservedQueue;

    @Value("${app.rabbitmq.stock-reservation-failed-queue}")
    private String stockReservationFailedQueue;

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

    // Crear queues separadas
    @Bean
    public Queue paymentProcessedQueue() {
        return new Queue(paymentProcessedQueue, true);
    }

    @Bean
    public Queue paymentFailedQueue() {
        return new Queue(paymentFailedQueue, true);
    }

    @Bean
    public Queue stockReservedQueue() {
        return new Queue(stockReservedQueue, true);
    }

    @Bean
    public Queue stockReservationFailedQueue() {
        return new Queue(stockReservationFailedQueue, true);
    }

    // Bindings para queues separadas
    @Bean
    public Binding bindingPaymentProcessed() {
        return BindingBuilder
                .bind(paymentProcessedQueue())
                .to(eventsExchange())
                .with(PAYMENT_PROCESSED_KEY)
                .noargs();
    }
    
    @Bean
    public Binding bindingPaymentFailed() {
        return BindingBuilder
                .bind(paymentFailedQueue())
                .to(eventsExchange())
                .with(PAYMENT_FAILED_KEY)
                .noargs();
    }
    
    @Bean
    public Binding bindingStockReserved() {
        return BindingBuilder
                .bind(stockReservedQueue())
                .to(eventsExchange())
                .with(STOCK_RESERVED_KEY)
                .noargs();
    }
    
    @Bean
    public Binding bindingStockReservationFailed() {
        return BindingBuilder
                .bind(stockReservationFailedQueue())
                .to(eventsExchange())
                .with(STOCK_RESERVATION_FAILED_KEY)
                .noargs();
    }
}