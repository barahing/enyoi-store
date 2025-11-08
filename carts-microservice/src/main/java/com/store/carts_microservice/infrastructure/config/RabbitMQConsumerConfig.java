package com.store.carts_microservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.store.common.messaging.MessagingConstants;

@Configuration
public class RabbitMQConsumerConfig {
    
    // Queue Names
    public static final String CART_CREATION_QUEUE_NAME = "user.created.queue";
    public static final String USER_DEACTIVATED_QUEUE_NAME = "user.deactivated.queue";
    public static final String USER_ACTIVATED_QUEUE_NAME = "user.activated.queue";
    
    // Exchange Names
    private static final String USER_EXCHANGE_NAME = MessagingConstants.USER_EXCHANGE;

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE_NAME);
    }

    @Bean
    @Qualifier("cartCreationQueue")  // ← AGREGAR QUALIFIER
    public Queue cartCreationQueue() {
        return new Queue(CART_CREATION_QUEUE_NAME, true); 
    }

    @Bean
    @Qualifier("userDeactivatedQueue")  // ← AGREGAR QUALIFIER
    public Queue userDeactivatedQueue() {
        return new Queue(USER_DEACTIVATED_QUEUE_NAME, true);
    }

    @Bean
    @Qualifier("userActivatedQueue")  // ← AGREGAR QUALIFIER
    public Queue userActivatedQueue() {
        return new Queue(USER_ACTIVATED_QUEUE_NAME, true);
    }

    @Bean
    public Binding cartCreationBinding(
            @Qualifier("cartCreationQueue") Queue cartCreationQueue,  // ← ESPECIFICAR QUALIFIER
            @Qualifier("userExchange") TopicExchange userExchange) {
        return BindingBuilder
                .bind(cartCreationQueue)
                .to(userExchange)
                .with(MessagingConstants.USER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding userDeactivatedBinding(
            @Qualifier("userDeactivatedQueue") Queue userDeactivatedQueue,  // ← ESPECIFICAR QUALIFIER
            @Qualifier("userExchange") TopicExchange userExchange) {
        return BindingBuilder
                .bind(userDeactivatedQueue)
                .to(userExchange)
                .with(MessagingConstants.USER_DEACTIVATED_ROUTING_KEY);
    }

    @Bean
    public Binding userActivatedBinding(
            @Qualifier("userActivatedQueue") Queue userActivatedQueue,  // ← ESPECIFICAR QUALIFIER
            @Qualifier("userExchange") TopicExchange userExchange) {
        return BindingBuilder
                .bind(userActivatedQueue)
                .to(userExchange)
                .with(MessagingConstants.USER_ACTIVATED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}