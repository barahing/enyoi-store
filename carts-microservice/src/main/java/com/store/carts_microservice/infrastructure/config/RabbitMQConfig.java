package com.store.carts_microservice.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.store.common.messaging.MessagingConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final ConnectionFactory connectionFactory;

    private static final String USER_EXCHANGE = MessagingConstants.USER_EXCHANGE; 
    private static final String Q_USER_CREATED = "user.created.queue.carts";
    private static final String Q_USER_DEACTIVATED = "user.deactivated.queue";
    private static final String Q_USER_ACTIVATED = "user.activated.queue";
    private static final String Q_STOCK_RESERVED = "stock.reserved.queue";
    private static final String Q_ORDER_CREATED = "order.created.queue";

    @Value("${app.rabbitmq.exchange:store.events}")
    private String eventsExchangeName; 

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate tpl = new RabbitTemplate(connectionFactory);
        tpl.setMessageConverter(jsonMessageConverter());
        return tpl;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); 
        factory.setDefaultRequeueRejected(false); 
        return factory;
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Declarables userAndEventTopology() {
        log.info("🐇 Declaring topology for carts-service …");

        TopicExchange userEx = new TopicExchange(USER_EXCHANGE, true, false);
        TopicExchange eventsEx = new TopicExchange(eventsExchangeName, true, false);

        Queue qUserCreated = QueueBuilder.durable(Q_USER_CREATED).build();
        Queue qUserDeactivated = QueueBuilder.durable(Q_USER_DEACTIVATED).build();
        Queue qUserActivated = QueueBuilder.durable(Q_USER_ACTIVATED).build();
        Queue qStockReserved = QueueBuilder.durable(Q_STOCK_RESERVED).build();
        Queue qOrderCreated = QueueBuilder.durable(Q_ORDER_CREATED).build();

        Binding bUserCreated = BindingBuilder.bind(qUserCreated)
                .to(userEx).with(MessagingConstants.USER_CREATED_ROUTING_KEY);
        Binding bUserDeactivated = BindingBuilder.bind(qUserDeactivated)
                .to(userEx).with(MessagingConstants.USER_DEACTIVATED_ROUTING_KEY);
        Binding bUserActivated = BindingBuilder.bind(qUserActivated)
                .to(userEx).with(MessagingConstants.USER_ACTIVATED_ROUTING_KEY);
        Binding bStockReserved = BindingBuilder.bind(qStockReserved)
                .to(eventsEx).with("stock.reserved");
        Binding bOrderCreated = BindingBuilder.bind(qOrderCreated)
                .to(eventsEx).with("order.created");

        return new Declarables(
                userEx, eventsEx,
                qUserCreated, qUserDeactivated, qUserActivated,
                qStockReserved, qOrderCreated, bUserCreated, 
                bUserDeactivated, bUserActivated, bStockReserved, bOrderCreated
        );
    }
}
