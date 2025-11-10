package com.store.orders_microservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String eventsExchangeName;
    
    @Value("${app.rabbitmq.payment-processed-queue}")
    private String paymentProcessedQueue;

    @Value("${app.rabbitmq.payment-failed-queue}")
    private String paymentFailedQueue;

    @Value("${app.rabbitmq.stock-reserved-queue}")
    private String stockReservedQueue;

    @Value("${app.rabbitmq.stock-reservation-failed-queue}")
    private String stockReservationFailedQueue;

    @Value("${app.rabbitmq.cart-converted-queue}")
    private String cartConvertedQueue;

    // ✅ NUEVA: Queue para publicar ProcessPaymentCommand
    @Value("${app.rabbitmq.payment-queue}")
    private String paymentQueue;

    @Value("${app.rabbitmq.order-shipped-queue:order.shipped.queue}")
    private String orderShippedQueue;

    @Value("${app.rabbitmq.order-delivered-queue:order.delivered.queue}")
    private String orderDeliveredQueue;

    // Routing Keys
    private static final String PAYMENT_PROCESSED_KEY = "payment.processed"; 
    private static final String PAYMENT_FAILED_KEY = "payment.failed"; 
    private static final String STOCK_RESERVED_KEY = "stock.reserved"; 
    private static final String STOCK_RESERVATION_FAILED_KEY = "stock.failed"; 
    private static final String CART_CONVERTED_KEY = "cart.converted.event";
    // ✅ NUEVA: Routing key para ProcessPaymentCommand
    private static final String PROCESS_PAYMENT_KEY = "payment.process";
    private static final String ORDER_SHIPPED_KEY = "order.shipped";
    private static final String ORDER_DELIVERED_KEY = "order.delivered";

    @Bean
    public Exchange eventsExchange() {
        return ExchangeBuilder
                .topicExchange(eventsExchangeName)
                .durable(true)
                .build();
    }

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

    @Bean
    public Queue cartConvertedQueue() {
        return new Queue(cartConvertedQueue, true);
    }

    // ✅ NUEVA: Queue para publicar ProcessPaymentCommand
    @Bean
    public Queue paymentQueue() {
        return new Queue(paymentQueue, true);
    }

    @Bean
    public Queue orderShippedQueue() {
        return new Queue(orderShippedQueue, true);
    }

    @Bean
    public Queue orderDeliveredQueue() {
        return new Queue(orderDeliveredQueue, true);
    }

    // Bindings existentes...
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

    @Bean
    public Binding bindingCartConverted() {
        return BindingBuilder
                .bind(cartConvertedQueue())
                .to(eventsExchange())
                .with(CART_CONVERTED_KEY)
                .noargs();
    }

    // ✅ NUEVO: Binding para publicar ProcessPaymentCommand
    @Bean
    public Binding bindingProcessPayment() {
        return BindingBuilder
                .bind(paymentQueue())
                .to(eventsExchange())
                .with(PROCESS_PAYMENT_KEY)
                .noargs();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Binding bindingOrderShipped() {
        return BindingBuilder
            .bind(orderShippedQueue())
            .to(eventsExchange())
            .with(ORDER_SHIPPED_KEY)
            .noargs();
    }

    @Bean
    public Binding bindingOrderDelivered() {
        return BindingBuilder
            .bind(orderDeliveredQueue())
            .to(eventsExchange())
            .with(ORDER_DELIVERED_KEY)
            .noargs();
    }
}