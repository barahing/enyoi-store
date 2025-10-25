// products-microservice/src/main/java/com/store/products_microservice/infrastructure/config/RabbitMQConsumerConfig.java

package com.store.products_microservice.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumerConfig {

    public static final String STOCK_EXCHANGE_NAME = "stock-exchange";
    public static final String RESERVE_STOCK_QUEUE = "reserve-stock-queue";
    public static final String RESERVE_STOCK_ROUTING_KEY = "stock.reserve.command";

    @Bean
    public TopicExchange stockExchange() {
        return new TopicExchange(STOCK_EXCHANGE_NAME);
    }

    @Bean
    public Queue reserveStockQueue() {
        // La cola se crea durable
        return new Queue(RESERVE_STOCK_QUEUE, true); 
    }

    @Bean
    public Binding reserveStockBinding() {
        return BindingBuilder.bind(reserveStockQueue())
                .to(stockExchange())
                .with(RESERVE_STOCK_ROUTING_KEY);
    }
}