package com.store.payments_microservice.domain.ports.out;

import com.store.common.events.PaymentFailedEvent;
import com.store.common.events.PaymentProcessedEvent;
import reactor.core.publisher.Mono;

public interface IEventPublisherPort {
    
    Mono<Void> publishPaymentProcessedEvent(PaymentProcessedEvent event);
    Mono<Void> publishPaymentFailedEvent(PaymentFailedEvent event);
}