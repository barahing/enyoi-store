package com.store.purchases_microservice.domain.ports.out;

import com.store.common.events.StockReceivedEvent;
import reactor.core.publisher.Mono;

public interface IEventPublisherPorts {
    Mono<Void> publishStockReceivedEvent(StockReceivedEvent event);
}