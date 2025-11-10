package com.store.payments_microservice.infrastructure.messaging.publisher;

import com.store.common.events.PaymentFailedEvent;
import com.store.common.events.PaymentProcessedEvent;
import com.store.payments_microservice.domain.ports.out.IEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitEventPublisherAdapter implements IEventPublisherPort {

    private final AmqpTemplate amqpTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String eventsExchange;

    private static final String KEY_PAYMENT_PROCESSED = "payment.processed";
    private static final String KEY_PAYMENT_FAILED = "payment.failed";

    private Mono<Void> sendEvent(String routingKey, Object event) {
        return Mono.fromRunnable(() -> {
            amqpTemplate.convertAndSend(eventsExchange, routingKey, event);
            log.info("💰 [PAYMENTS] Event sent: {} with Routing Key: {}", 
                    event.getClass().getSimpleName(), routingKey);
        }).then();
    }

    @Override
    public Mono<Void> publishPaymentProcessedEvent(PaymentProcessedEvent event) {
        log.info("💰 [PAYMENTS] ⚡⚡⚡ Publishing PaymentProcessedEvent for orderId: {}", event.getOrderId());
        
        return Mono.fromRunnable(() -> {
            try {
                log.info("💰 [PAYMENTS] Sending to exchange: {}, routingKey: {}", eventsExchange, KEY_PAYMENT_PROCESSED);
                amqpTemplate.convertAndSend(eventsExchange, KEY_PAYMENT_PROCESSED, event);
                log.info("💰 [PAYMENTS] ⚡⚡⚡ PaymentProcessedEvent SENT successfully for orderId: {}", event.getOrderId());
            } catch (Exception e) {
                log.error("❌ [PAYMENTS] Error sending PaymentProcessedEvent: {}", e.getMessage(), e);
                throw e;
            }
        }).then();
    }

    @Override
    public Mono<Void> publishPaymentFailedEvent(PaymentFailedEvent event) {
        return sendEvent(KEY_PAYMENT_FAILED, event);
    }
}