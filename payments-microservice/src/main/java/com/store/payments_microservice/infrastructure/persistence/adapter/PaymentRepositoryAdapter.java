package com.store.payments_microservice.infrastructure.persistence.adapter;

import org.springframework.stereotype.Repository;

import com.store.payments_microservice.domain.model.Payment;
import com.store.payments_microservice.domain.ports.out.IPaymentRepositoryPort;
import com.store.payments_microservice.infrastructure.persistence.mapper.IPaymentEntityMapper;
import com.store.payments_microservice.infrastructure.persistence.repository.IPaymentRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements IPaymentRepositoryPort {

    private final IPaymentRepository r2dbcRepository; 
    private final IPaymentEntityMapper mapper; 

    @Override
    public Mono<Payment> create(Payment payment) {
        return Mono.just(payment)
            .map(mapper::toEntity)
            .flatMap(entity -> {
                entity.setId(null); 
                return r2dbcRepository.save(entity);
            })
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Payment> update(Payment payment) {
        return Mono.just(payment)
            .map(mapper::toEntity)
            .flatMap(r2dbcRepository::save) 
            .map(mapper::toDomain);
    }

    @Override
    public Mono<Payment> findByOrderId(UUID orderId) {
        return r2dbcRepository.findByOrderId(orderId)
                .map(mapper::toDomain)
                .switchIfEmpty(Mono.empty());
    }
}
