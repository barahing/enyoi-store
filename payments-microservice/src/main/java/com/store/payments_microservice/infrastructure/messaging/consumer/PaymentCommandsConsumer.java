package com.store.payments_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.store.common.commands.ProcessPaymentCommand; 
import com.store.payments_microservice.domain.ports.in.IPaymentServicePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCommandsConsumer { 

    private final IPaymentServicePort paymentServicePort;

    @RabbitListener(queues = "${app.rabbitmq.payment-queue}")
    public void handleProcessPaymentCommand(ProcessPaymentCommand command) {
        log.info("Received ProcessPaymentCommand for Order ID: {}. Initiating payment processing.", command.orderId());

        paymentServicePort.processOrderPayment(command) 
            .subscribeOn(Schedulers.boundedElastic()) 
            .doOnError(e -> log.error("Error processing payment for Order ID {}: {}", command.orderId(), e.getMessage()))
            .subscribe(); 
    }
}