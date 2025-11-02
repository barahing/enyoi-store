package com.store.payments_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.store.payments_microservice.domain.ports.in.IPaymentServicePort;
import com.store.common.events.StockReservedEvent; 

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventsConsumer {

    private final IPaymentServicePort paymentServicePort;

    @RabbitListener(queues = "${app.rabbitmq.stock-reserved-queue}")
    public void handleStockReserved(StockReservedEvent event) {
        log.info("Received StockReservedEvent for Order ID: {}. Initiating payment process.", event.orderId());

        paymentServicePort.processOrderPayment(event.orderId()) 
            .subscribeOn(Schedulers.boundedElastic()) 
            .doOnError(e -> log.error("Error processing payment for Order ID {}: {}", event.orderId(), e.getMessage()))
            .subscribe(); 
    }
}