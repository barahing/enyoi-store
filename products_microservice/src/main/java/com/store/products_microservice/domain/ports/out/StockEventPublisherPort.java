package com.store.products_microservice.domain.ports.out;

import com.store.common.events.StockReservationFailedEvent;
import com.store.common.events.StockReservedEvent;
import reactor.core.publisher.Mono;

public interface StockEventPublisherPort {
    Mono<Void> publishStockReserved(StockReservedEvent event);
    Mono<Void> publishStockReservationFailed(StockReservationFailedEvent event);
}