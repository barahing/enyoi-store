package com.store.purchases_microservice.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.events-exchange}")
    private String eventsExchangeName;

    @Value("${app.rabbitmq.purchases-queue}")
    private String purchasesQueueName;

    private static final String LOW_STOCK_ALERT_ROUTING_KEY = "stock.low";

    // 🧩 --- Exchange principal ---
    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(eventsExchangeName).durable(true).build();
    }

    // 📦 --- Queue para el servicio de compras ---
    @Bean
    public Queue purchasesQueue() {
        log.info("📦 [PURCHASES] Declaring queue: {}", purchasesQueueName);
        return QueueBuilder.durable(purchasesQueueName).build();
    }

    // 🔗 --- Binding entre cola y exchange ---
    @Bean
    public Binding bindingPurchasesQueue(Queue purchasesQueue, TopicExchange eventsExchange) {
        log.info("🔗 [PURCHASES] Binding '{}' with key '{}'", purchasesQueue.getName(), LOW_STOCK_ALERT_ROUTING_KEY);
        return BindingBuilder
                .bind(purchasesQueue)
                .to(eventsExchange)
                .with(LOW_STOCK_ALERT_ROUTING_KEY);
    }

    // 🧠 --- Conversor JSON para eventos ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 🐇 --- Template de envío de mensajes ---
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setExchange(eventsExchangeName);
        // Activa confirmaciones para depurar fácilmente
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.debug("✅ [RABBITMQ] Message published successfully: {}", correlationData);
            } else {
                log.error("❌ [RABBITMQ] Failed to publish message: {}", cause);
            }
        });
        return template;
    }

    // 🧏 --- Factory para listeners ---
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    // 🧩 --- Admin opcional (crea colas automáticamente) ---
    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}
