package com.store.payments_microservice.infrastructure.persistence.repository;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.store.payments_microservice.domain.model.Payment;


public interface IPaymentRepository extends ReactiveCrudRepository<Payment, UUID> {
    
}
