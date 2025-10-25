package com.store.products_microservice.infrastructure.adapters.inbound.rabbitmq;

import com.store.products_microservice.domain.ports.in.IStockManagementPort;
import com.store.common.events.ReserveStockCommand;
import com.store.products_microservice.infrastructure.config.RabbitMQConsumerConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockRabbitListener {

    private final IStockManagementPort stockManagementPort;

    @RabbitListener(queues = RabbitMQConsumerConfig.RESERVE_STOCK_QUEUE)
    public void handleReserveStockCommand(ReserveStockCommand command) {
        
        stockManagementPort.handleStockReservation(command)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSuccess(v -> log.info("Stock reservation processed successfully for Order ID: {}", command.orderId()))
                .doOnError(e -> log.error("Failed to process stock reservation for Order ID: {}. Error: {}", command.orderId(), e.getMessage()))
                .subscribe();
    }
}