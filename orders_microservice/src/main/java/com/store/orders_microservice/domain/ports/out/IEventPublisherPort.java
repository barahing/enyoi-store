package com.store.orders_microservice.domain.ports.out;

import com.store.common.commands.ProcessPaymentCommand;
import com.store.common.commands.ReleaseStockCommand;
import com.store.common.commands.ReserveStockCommand;
import com.store.common.events.OrderCancelledEvent;
import com.store.common.events.OrderCreatedEvent;
import com.store.common.events.OrderConfirmedEvent;
import reactor.core.publisher.Mono;

public interface IEventPublisherPort {

    Mono<Void> publishOrderCreatedEvent(OrderCreatedEvent event);

    Mono<Void> publishOrderConfirmedEvent(OrderConfirmedEvent event);
    
    Mono<Void> publishOrderCancelledEvent(OrderCancelledEvent event);
 
    Mono<Void> publishReleaseStockCommand(ReleaseStockCommand command);

    Mono<Void> publishProcessPaymentCommand(ProcessPaymentCommand command);

    Mono<Void> publishReserveStockCommand(ReserveStockCommand command); 
}
