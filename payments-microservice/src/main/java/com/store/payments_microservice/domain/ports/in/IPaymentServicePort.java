package com.store.payments_microservice.domain.ports.in;

import com.store.common.commands.ProcessPaymentCommand;

import reactor.core.publisher.Mono;

public interface IPaymentServicePort {
    Mono<Void> processOrderPayment(ProcessPaymentCommand command);
}