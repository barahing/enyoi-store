package com.store.payments_microservice.infrastructure.persistence.repository;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.store.payments_microservice.domain.model.Payment;
import com.store.payments_microservice.infrastructure.persistence.entity.PaymentEntity;

import reactor.core.publisher.Mono;


public interface IPaymentRepository extends ReactiveCrudRepository<PaymentEntity, UUID> {
    Mono<PaymentEntity> findByOrderId(UUID orderId);
}
