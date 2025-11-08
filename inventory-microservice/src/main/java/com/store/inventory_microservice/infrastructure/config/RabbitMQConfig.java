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

    @Value("${app.rabbitmq.exchange}")
    private String eventsExchangeName;

    // Colas de Consumo
    @Value("${app.rabbitmq.order-created-queue}")
    private String orderCreatedQueueName;

    @Value("${app.rabbitmq.release-stock-command-queue}")
    private String releaseStockQueueName;

    @Value("${app.rabbitmq.payment-processed-queue}") 
    private String paymentProcessedQueueName;

    // NUEVA: Cola para ReserveStockCommand
    @Value("${app.rabbitmq.reserve-stock-command-queue}")
    private String reserveStockCommandQueueName;

    @Bean
    public Exchange eventsExchange() {
        return ExchangeBuilder
                .topicExchange(eventsExchangeName)
                .durable(true)
                .build();
    }
    
    // --- Queues que Inventory Consume ---
    
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(orderCreatedQueueName, true); 
    }

    @Bean
    public Queue releaseStockCommandQueue() {
        return new Queue(releaseStockQueueName, true);
    }
    
    @Bean
    public Queue paymentProcessedQueue() {
        return new Queue(paymentProcessedQueueName, true); 
    }
    
    // NUEVA: Queue para ReserveStockCommand
    @Bean
    public Queue reserveStockCommandQueue() {
        return new Queue(reserveStockCommandQueueName, true);
    }
    
    // --- Bindings para Consumir Eventos ---

    // 1. OrderCreatedEvent (para reserva inicial de stock)
    @Bean
    public Binding bindingOrderCreatedQueueToExchange() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(eventsExchange())
                .with("order.created") 
                .noargs();
    }

    // 2. ReserveStockCommand (comando directo para reservar stock)
    @Bean
    public Binding bindingReserveStockCommandQueueToExchange() {
        return BindingBuilder
                .bind(reserveStockCommandQueue())
                .to(eventsExchange())
                .with("stock.reserve") 
                .noargs();
    }

    // 3. ReleaseStockCommand (Rollback)
    @Bean
    public Binding bindingReleaseStockCommandQueueToExchange() {
        return BindingBuilder
                .bind(releaseStockCommandQueue())
                .to(eventsExchange())
                .with("stock.release") 
                .noargs();
    }
    
    // 4. PaymentProcessedEvent (Confirmación - Deducción final del stock)
    @Bean
    public Binding bindingPaymentProcessedQueueToExchange() {
        return BindingBuilder
                .bind(paymentProcessedQueue())
                .to(eventsExchange())
                .with("payment.processed") 
                .noargs();
    }
}