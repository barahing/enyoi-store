package com.store.users_microservice.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitMQProducerConfig {

    public static final String USER_EXCHANGE_NAME = "user.exchange";

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE_NAME, true, false);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);

        template.setReturnsCallback(ret -> {
            log.error("❌ [USERS] RETURN (unroutable). replyCode={} replyText={} exchange={} routingKey={} message={}",
                    ret.getReplyCode(), ret.getReplyText(), ret.getExchange(), ret.getRoutingKey(), ret.getMessage());
        });

        template.setConfirmCallback((correlation, ack, cause) -> {
            if (ack) {
                log.info("✅ [USERS] CONFIRM OK (accepted by exchange). correlationId={}", 
                        correlation != null ? correlation.getId() : "null");
            } else {
                log.error("❌ [USERS] CONFIRM FAIL (NOT accepted by exchange). cause={}", cause);
            }
        });

        return template;
    }
}
