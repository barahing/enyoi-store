package com.store.carts_microservice.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.store.common.messaging.MessagingConstants;

@Configuration
public class RabbitMQConfig {

    // --- EXCHANGES ---
    @Value("${app.rabbitmq.exchange:store.events}")
    private String eventsExchangeName;

    private static final String USER_EXCHANGE_NAME = MessagingConstants.USER_EXCHANGE;

    // --- QUEUE NAMES ---
    private static final String CART_CREATION_QUEUE = "user.created.queue";
    private static final String USER_DEACTIVATED_QUEUE = "user.deactivated.queue";
    private static final String USER_ACTIVATED_QUEUE = "user.activated.queue";
    private static final String ORDER_CONFIRMED_QUEUE = "order.confirmed.queue"; // 👈 NUEVA

    // --- BEANS ---

    // ✅ JSON Converter (único)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ✅ RabbitTemplate (para publicar mensajes)
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // ✅ ListenerContainerFactory (para consumir mensajes)
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // --- EXCHANGES ---
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE_NAME);
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(eventsExchangeName);
    }

    // --- QUEUES ---
    @Bean
    public Queue cartCreationQueue() {
        return new Queue(CART_CREATION_QUEUE, true);
    }

    @Bean
    public Queue userDeactivatedQueue() {
        return new Queue(USER_DEACTIVATED_QUEUE, true);
    }

    @Bean
    public Queue userActivatedQueue() {
        return new Queue(USER_ACTIVATED_QUEUE, true);
    }

    @Bean
    public Queue orderConfirmedQueue() {
        return new Queue(ORDER_CONFIRMED_QUEUE, true);
    }

    @Bean
    public Queue stockReservedQueue() {
        System.out.println("🔧 [CARTS] Creating queue: stock.reserved.queue");
        return new Queue("stock.reserved.queue", true);
    }

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue("order.created.queue", true);
    }


    // --- BINDINGS (user events) ---
    @Bean
    public Binding cartCreationBinding() {
        return BindingBuilder
                .bind(cartCreationQueue())
                .to(userExchange())
                .with(MessagingConstants.USER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding userDeactivatedBinding() {
        return BindingBuilder
                .bind(userDeactivatedQueue())
                .to(userExchange())
                .with(MessagingConstants.USER_DEACTIVATED_ROUTING_KEY);
    }

    @Bean
    public Binding userActivatedBinding() {
        return BindingBuilder
                .bind(userActivatedQueue())
                .to(userExchange())
                .with(MessagingConstants.USER_ACTIVATED_ROUTING_KEY);
    }

    // --- BINDING (order confirmed event) ---
    @Bean
    public Binding orderConfirmedBinding() {
        return BindingBuilder
                .bind(orderConfirmedQueue())
                .to(eventsExchange())
                .with("order.confirmed");
    }
    @Bean
    public Binding bindingStockReservedQueueToExchange(
            @Qualifier("stockReservedQueue") Queue stockReservedQueue,
            @Qualifier("userExchange") TopicExchange userExchange) {
        System.out.println("🔧 [CARTS] Binding stock.reserved.queue to exchange store.events with key stock.reserved");
        return BindingBuilder
                .bind(stockReservedQueue)
                .to(userExchange)
                .with("stock.reserved"); // 👈 debe coincidir con routing key usada por Inventory
    }

    @Bean
    public Binding orderCreatedBinding(
            @Qualifier("orderCreatedQueue") Queue orderCreatedQueue,
            @Qualifier("eventsExchange") TopicExchange eventsExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(eventsExchange)
                .with("order.created");
    }
}
