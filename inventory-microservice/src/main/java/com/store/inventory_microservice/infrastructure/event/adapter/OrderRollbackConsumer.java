package com.store.inventory_microservice.infrastructure.event.adapter;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.store.common.commands.ReleaseStockCommand;
import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderRollbackConsumer {

    private final IProductStockServicePort productStockServicePort;

    @RabbitListener(queues = "${app.rabbitmq.release-stock-command-queue}") 
    public void handleReleaseStockCommand(ReleaseStockCommand command) {
        log.warn("Received ReleaseStockCommand for Order ID: {}. Initiating stock rollback. Reason: {}", 
            command.getOrderId(), command.getReason());

        productStockServicePort.releaseOrderStock(command.getOrderId(), command.getProducts())
            .subscribeOn(Schedulers.boundedElastic()) 
            .doOnSuccess(v -> log.info("Successfully released stock for Order ID: {}", command.getOrderId()))
            .doOnError(e -> log.error("Error during stock release (ROLLBACK) for Order ID {}: {}", 
                command.getOrderId(), e.getMessage()))
            .subscribe(); 
    }
}