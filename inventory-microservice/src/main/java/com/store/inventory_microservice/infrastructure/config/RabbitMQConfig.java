package com.store.inventory_microservice.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchange}")
    private String eventsExchangeName;

    @Value("${app.rabbitmq.order-created-queue}")
    private String orderCreatedQueueName;

    @Value("${app.rabbitmq.release-stock-command-queue}")
    private String releaseStockQueueName;

    @Value("${app.rabbitmq.payment-processed-queue}")
    private String paymentProcessedQueueName;

    @Value("${app.rabbitmq.reserve-stock-command-queue}")
    private String reserveStockCommandQueueName;

    @Value("${app.rabbitmq.order-cancelled-queue}")
    private String orderCancelledQueueName;

    @Value("${app.rabbitmq.stock-received-queue}")
    private String stockReceivedQueueName;

    @Value("${app.rabbitmq.order-created-key}")
    private String orderCreatedKey;

    @Value("${app.rabbitmq.stock-reserve-key}")
    private String stockReserveKey;

    @Value("${app.rabbitmq.stock-release-key}")
    private String stockReleaseKey;

    @Value("${app.rabbitmq.payment-processed-key}")
    private String paymentProcessedKey;

    @Value("${app.rabbitmq.order-cancelled-key}")
    private String orderCancelledKey;

    @Value("${app.rabbitmq.stock-received-key}")
    private String stockReceivedKey;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Exchange eventsExchange() {
        return ExchangeBuilder.topicExchange(eventsExchangeName).durable(true).build();
    }

    @Bean public Queue orderCreatedQueue() { return new Queue(orderCreatedQueueName, true); }
    @Bean public Queue releaseStockCommandQueue() { return new Queue(releaseStockQueueName, true); }
    @Bean public Queue paymentProcessedQueue() { return new Queue(paymentProcessedQueueName, true); }
    @Bean public Queue reserveStockCommandQueue() { return new Queue(reserveStockCommandQueueName, true); }
    @Bean public Queue orderCancelledQueue() { return new Queue(orderCancelledQueueName, true); }
    @Bean public Queue stockReceivedQueue() { return new Queue(stockReceivedQueueName, true); }
    @Bean public Queue lowStockAlertQueue() { return new Queue("inventory.lowstock.alert.queue", true);
}
    @Bean
    public Binding bindingOrderCreatedQueueToExchange() {
        return BindingBuilder.bind(orderCreatedQueue()).to(eventsExchange()).with(orderCreatedKey).noargs();
    }

    @Bean
    public Binding bindingReserveStockCommandQueueToExchange() {
        return BindingBuilder.bind(reserveStockCommandQueue()).to(eventsExchange()).with(stockReserveKey).noargs();
    }

    @Bean
    public Binding bindingReleaseStockCommandQueueToExchange() {
        return BindingBuilder.bind(releaseStockCommandQueue()).to(eventsExchange()).with(stockReleaseKey).noargs();
    }

    @Bean
    public Binding bindingPaymentProcessedQueueToExchange() {
        return BindingBuilder.bind(paymentProcessedQueue()).to(eventsExchange()).with(paymentProcessedKey).noargs();
    }

    @Bean
    public Binding bindingOrderCancelledQueueToExchange() {
        return BindingBuilder.bind(orderCancelledQueue()).to(eventsExchange()).with(orderCancelledKey).noargs();
    }

    @Bean
    public Binding bindingStockReceivedQueueToExchange() {
        return BindingBuilder.bind(stockReceivedQueue()).to(eventsExchange()).with(stockReceivedKey).noargs();
    }

    @Bean
    public Binding bindLowStockAlertQueue(
            @Qualifier("lowStockAlertQueue") Queue lowStockAlertQueue,
            @Qualifier("eventsExchange") Exchange eventsExchange) {

        return BindingBuilder.bind(lowStockAlertQueue).to(eventsExchange).with("inventory.lowstock.alert").noargs();
    }

}
