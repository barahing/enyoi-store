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

    @Value("${app.rabbitmq.payment-queue}")
    private String paymentQueue;

    @Value("${app.rabbitmq.payment-processed-key}")
    private String paymentProcessedKey;
    @Value("${app.rabbitmq.payment-failed-key}")
    private String paymentFailedKey;
    @Value("${app.rabbitmq.stock-reserved-key}")
    private String stockReservedKey;
    @Value("${app.rabbitmq.stock-reservation-failed-key}")
    private String stockReservationFailedKey;
    @Value("${app.rabbitmq.cart-converted-key}")
    private String cartConvertedKey;
    @Value("${app.rabbitmq.process-payment-key}")
    private String processPaymentKey;

    @Bean
    public Exchange eventsExchange() {
        return ExchangeBuilder.topicExchange(eventsExchangeName).durable(true).build();
    }

    @Bean public Queue paymentProcessedQueue() { return new Queue(paymentProcessedQueue, true); }
    @Bean public Queue paymentFailedQueue() { return new Queue(paymentFailedQueue, true); }
    @Bean public Queue stockReservedQueue() { return new Queue(stockReservedQueue, true); }
    @Bean public Queue stockReservationFailedQueue() { return new Queue(stockReservationFailedQueue, true); }
    @Bean public Queue cartConvertedQueue() { return new Queue(cartConvertedQueue, true); }
    @Bean public Queue paymentQueue() { return new Queue(paymentQueue, true); }

    @Bean public Binding bindingPaymentProcessed() {
        return BindingBuilder.bind(paymentProcessedQueue())
            .to(eventsExchange())
            .with(paymentProcessedKey)
            .noargs();
    }

    @Bean public Binding bindingPaymentFailed() {
        return BindingBuilder.bind(paymentFailedQueue())
            .to(eventsExchange())
            .with(paymentFailedKey)
            .noargs();
    }

    @Bean public Binding bindingStockReserved() {
        return BindingBuilder.bind(stockReservedQueue())
            .to(eventsExchange())
            .with(stockReservedKey)
            .noargs();
    }

    @Bean public Binding bindingStockReservationFailed() {
        return BindingBuilder.bind(stockReservationFailedQueue())
            .to(eventsExchange())
            .with(stockReservationFailedKey)
            .noargs();
    }

    @Bean public Binding bindingCartConverted() {
        return BindingBuilder.bind(cartConvertedQueue())
            .to(eventsExchange())
            .with(cartConvertedKey)
            .noargs();
    }

    @Bean public Binding bindingProcessPayment() {
        return BindingBuilder.bind(paymentQueue())
            .to(eventsExchange())
            .with(processPaymentKey)
            .noargs();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
