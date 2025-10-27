package com.store.inventory_microservice.infrastructure.event.adapter;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.store.common.events.StockReservationFailedEvent;
import com.store.common.events.StockReservedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {
    
    private final RabbitTemplate rabbitTemplate; 

    public void publishStockReserved(UUID orderId) {
        log.info("Publishing StockReservedEvent for Order ID: {}", orderId);
        rabbitTemplate.convertAndSend("orders.exchange", "stock.reserved", new StockReservedEvent(orderId)); 
    }
    
    public void publishStockReservationFailed(UUID orderId, String reason) {
        log.error("Publishing StockReservationFailedEvent for Order ID: {} with reason: {}", orderId, reason);
        rabbitTemplate.convertAndSend("orders.exchange", "stock.failed", new StockReservationFailedEvent(orderId, reason));
    }
}