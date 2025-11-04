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

    // AÑADIDA: Cola para la confirmación de la reserva tras pago exitoso
    @Value("${app.rabbitmq.payment-processed-queue}") 
    private String paymentProcessedQueueName;

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
    
    // --- Bindings para Consumir Eventos ---

    // 1. Reserva de Stock
    @Bean
    public Binding bindingOrderCreatedQueueToExchange() {
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(eventsExchange())
                .with("order.created") 
                .noargs();
    }

    // 2. Rollback
    @Bean
    public Binding bindingReleaseStockCommandQueueToExchange() {
        return BindingBuilder
                .bind(releaseStockCommandQueue())
                .to(eventsExchange())
                .with("stock.release") 
                .noargs();
    }
    
    // 3. Confirmación (Deducción final del stock)
    @Bean
    public Binding bindingPaymentProcessedQueueToExchange() {
        return BindingBuilder
                .bind(paymentProcessedQueue())
                .to(eventsExchange())
                .with("payment.processed") 
                .noargs();
    }
}
