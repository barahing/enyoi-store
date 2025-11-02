package com.store.inventory_microservice.domain.ports.out;

import com.store.common.events.StockReservedEvent;
import com.store.common.events.StockReservationFailedEvent;
import reactor.core.publisher.Mono;

public interface IEventPublisherPort {

    Mono<Void> publishStockReservedEvent(StockReservedEvent event);

    Mono<Void> publishStockReservationFailedEvent(StockReservationFailedEvent event);
}