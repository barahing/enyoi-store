package com.store.payments_microservice.domain.ports.in;

import java.util.UUID;

import reactor.core.publisher.Mono;

public interface IPaymentServicePort {
    Mono<Void> processOrderPayment(UUID orderId);
}