package com.store.purchases_microservice.infrastructure.messaging.adapter;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.store.purchases_microservice.domain.ports.out.IEventPublisherPorts;
import com.store.common.events.StockReceivedEvent;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
public class EventPublisherAdapter implements IEventPublisherPorts {

    private final RabbitTemplate rabbitTemplate;
    
    private static final String EVENTS_EXCHANGE = "store.events"; 
    private static final String STOCK_RECEIVED_ROUTING_KEY = "stock.received";

    @Override
    public Mono<Void> publishStockReceivedEvent(StockReceivedEvent event) {
        return Mono.fromRunnable(() -> 
            rabbitTemplate.convertAndSend(
                EVENTS_EXCHANGE, 
                STOCK_RECEIVED_ROUTING_KEY, 
                event
            )
        ).subscribeOn(Schedulers.boundedElastic()).then();
    }
}