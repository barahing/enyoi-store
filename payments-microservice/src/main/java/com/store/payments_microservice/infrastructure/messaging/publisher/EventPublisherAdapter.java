package com.store.payments_microservice.infrastructure.messaging.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.store.payments_microservice.domain.ports.out.IEventPublisherPort;
import com.store.common.events.PaymentFailedEvent;
import com.store.common.events.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class EventPublisherAdapter implements IEventPublisherPort {

    private final RabbitTemplate rabbitTemplate;
    
    private static final String EVENTS_EXCHANGE = "store.events"; 
    
    private static final String PAYMENT_PROCESSED_ROUTING_KEY = "payment.processed";
    private static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";

    @Override
    public Mono<Void> publishPaymentProcessedEvent(PaymentProcessedEvent event) {
        return Mono.fromRunnable(() -> 
            rabbitTemplate.convertAndSend(
                EVENTS_EXCHANGE, 
                PAYMENT_PROCESSED_ROUTING_KEY, 
                event
            )
        ).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishPaymentFailedEvent(PaymentFailedEvent event) {
        return Mono.fromRunnable(() -> 
            rabbitTemplate.convertAndSend(
                EVENTS_EXCHANGE, 
                PAYMENT_FAILED_ROUTING_KEY, 
                event
            )
        ).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
