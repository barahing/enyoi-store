package com.store.payments_microservice.domain.ports.out;

import java.util.UUID;

import com.store.payments_microservice.domain.model.Payment;

import reactor.core.publisher.Mono;

public interface IPaymentRepositoryPort {
    
    Mono<Payment> save(Payment payment);
    Mono<Payment> findByOrderId(UUID orderId);
}