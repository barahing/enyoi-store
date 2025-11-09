package com.store.inventory_microservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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

    // ✅ BEAN CRÍTICO: MessageConverter
    @Bean
    public MessageConverter jsonMessageConverter() {
        System.out.println("🔧 [INVENTORY] Creating Jackson2JsonMessageConverter");
        return new Jackson2JsonMessageConverter();
    }

    // ✅ BEAN CRÍTICO: RabbitTemplate con el converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        System.out.println("🔧 [INVENTORY] RabbitTemplate configured with MessageConverter");
        return rabbitTemplate;
    }

    // ✅ BEAN CRÍTICO: ContainerFactory para los listeners
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        System.out.println("🔧 [INVENTORY] ListenerContainerFactory configured with MessageConverter");
        return factory;
    }

    @Bean
    public Exchange eventsExchange() {
        System.out.println("🔧 [INVENTORY] Creating exchange: " + eventsExchangeName);
        return ExchangeBuilder
                .topicExchange(eventsExchangeName)
                .durable(true)
                .build();
    }
    
    // --- Queues que Inventory Consume ---
    
    @Bean
    public Queue orderCreatedQueue() {
        System.out.println("🔧 [INVENTORY] Creating queue: " + orderCreatedQueueName);
        return new Queue(orderCreatedQueueName, true); 
    }

    @Bean
    public Queue releaseStockCommandQueue() {
        System.out.println("🔧 [INVENTORY] Creating queue: " + releaseStockQueueName);
        return new Queue(releaseStockQueueName, true);
    }
    
    @Bean
    public Queue paymentProcessedQueue() {
        System.out.println("🔧 [INVENTORY] Creating queue: " + paymentProcessedQueueName);
        return new Queue(paymentProcessedQueueName, true); 
    }
    
    // NUEVA: Queue para ReserveStockCommand
    @Bean
    public Queue reserveStockCommandQueue() {
        System.out.println("🔧 [INVENTORY] Creating queue: " + reserveStockCommandQueueName);
        return new Queue(reserveStockCommandQueueName, true);
    }
    
    // --- Bindings para Consumir Eventos ---

    // 1. OrderCreatedEvent (para reserva inicial de stock)
    @Bean
    public Binding bindingOrderCreatedQueueToExchange() {
        System.out.println("🔧 [INVENTORY] Binding order-created-queue to exchange");
        return BindingBuilder
                .bind(orderCreatedQueue())
                .to(eventsExchange())
                .with("order.created") 
                .noargs();
    }

    // 2. ReserveStockCommand (comando directo para reservar stock)
    @Bean
    public Binding bindingReserveStockCommandQueueToExchange() {
        System.out.println("🔧 [INVENTORY] Binding reserve-stock-command-queue to exchange");
        return BindingBuilder
                .bind(reserveStockCommandQueue())
                .to(eventsExchange())
                .with("stock.reserve") 
                .noargs();
    }

    // 3. ReleaseStockCommand (Rollback)
    @Bean
    public Binding bindingReleaseStockCommandQueueToExchange() {
        System.out.println("🔧 [INVENTORY] Binding release-stock-command-queue to exchange");
        return BindingBuilder
                .bind(releaseStockCommandQueue())
                .to(eventsExchange())
                .with("stock.release") 
                .noargs();
    }
    
    // 4. PaymentProcessedEvent (Confirmación - Deducción final del stock)
    @Bean
    public Binding bindingPaymentProcessedQueueToExchange() {
        System.out.println("🔧 [INVENTORY] Binding payment-processed-queue to exchange");
        return BindingBuilder
                .bind(paymentProcessedQueue())
                .to(eventsExchange())
                .with("payment.processed") 
                .noargs();
    }
}