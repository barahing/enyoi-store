package com.store.notifications_microservice.infrastructure.config;

import com.store.common.messaging.MessagingConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQConfig {

    private static final String GLOBAL_EXCHANGE = "store.events";

    @Bean
    public TopicExchange globalExchange() {
        return new TopicExchange(GLOBAL_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(MessagingConstants.USER_EXCHANGE, true, false);
    }

    @Bean public Queue orderCreatedQueue() { return new Queue("order.created.queue.notifications", true); }
    @Bean public Queue orderConfirmedQueue() { return new Queue("order.confirmed.queue", true); }
    @Bean public Queue paymentFailedQueue() { return new Queue(MessagingConstants.PAYMENT_FAILED_QUEUE, true); }
    @Bean public Queue shippingSentQueue() { return new Queue(MessagingConstants.SHIPPING_SENT_QUEUE, true); }
    @Bean public Queue userCreatedQueue() { return new Queue("user.created.queue.notifications", true);
}
    @Bean
    public Binding bindOrderCreated() {
        return BindingBuilder.bind(orderCreatedQueue())
                .to(globalExchange())
                .with(MessagingConstants.ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding bindOrderConfirmed() {
        return BindingBuilder.bind(orderConfirmedQueue())
                .to(globalExchange())
                .with(MessagingConstants.ORDER_CONFIRMED_ROUTING_KEY);
    }

    @Bean
    public Binding bindPaymentFailed() {
        return BindingBuilder.bind(paymentFailedQueue())
                .to(globalExchange())
                .with(MessagingConstants.PAYMENT_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding bindShippingSent() {
        return BindingBuilder.bind(shippingSentQueue())
                .to(globalExchange())
                .with(MessagingConstants.SHIPPING_SENT_ROUTING_KEY);
    }

    @Bean
    public Binding bindUserCreated() {
        return BindingBuilder.bind(userCreatedQueue())
                .to(userExchange())
                .with(MessagingConstants.USER_CREATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    private final ConnectionFactory connectionFactory;

    public RabbitMQConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }
}
