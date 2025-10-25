package com.store.carts_microservice.domain.ports.out;

import com.store.common.events.CartConvertedEvent;
import reactor.core.publisher.Mono;

public interface ICartEventPublisherPort {
    
    Mono<Void> publishCartConverted(CartConvertedEvent event);
}