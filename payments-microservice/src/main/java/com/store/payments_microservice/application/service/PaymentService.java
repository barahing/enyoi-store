package com.store.payments_microservice.application.service;

import java.util.UUID;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.store.payments_microservice.domain.model.Payment;
import com.store.payments_microservice.domain.ports.in.IPaymentServicePort;
import com.store.payments_microservice.domain.ports.out.IEventPublisherPort;
import com.store.payments_microservice.domain.ports.out.IPaymentRepositoryPort;
import com.store.common.commands.ProcessPaymentCommand; 
import com.store.common.events.PaymentFailedEvent;
import com.store.common.events.PaymentProcessedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; 
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j 
public class PaymentService implements IPaymentServicePort {

    private final IPaymentRepositoryPort persistencePort;
    private final IEventPublisherPort eventPublisherPort;
    private final Random random = new Random();

    @Override
    public Mono<Void> processOrderPayment(ProcessPaymentCommand command) {
        // 🔥 CAMBIO TEMPORAL: Siempre éxito para testing
        boolean paymentSuccessful = true; // En lugar de random.nextInt(100) < 80

        UUID orderId = command.orderId();
        String paymentMethod = command.paymentMethod();

        Payment payment = Payment.createNew(orderId, command.amount(), paymentMethod);

        log.info("Processing payment for Order ID: {} Amount: {}", orderId, command.amount());

        return persistencePort.create(payment)
            .flatMap(savedPayment -> {
                if (paymentSuccessful) {
                    String transactionRef = UUID.randomUUID().toString();
                    Payment processedPayment = savedPayment.markAsProcessed(transactionRef);

                    log.info("✅ Payment SUCCESS for Order ID {}. Transaction: {}", orderId, transactionRef);

                    return persistencePort.update(processedPayment)
                        .then(Mono.defer(() -> {
                            PaymentProcessedEvent successEvent = new PaymentProcessedEvent(
                                orderId,
                                processedPayment.getId(),
                                transactionRef
                            );
                            return eventPublisherPort.publishPaymentProcessedEvent(successEvent);
                        }));
                } else {
                    // 🔥 ESTO NO SE EJECUTARÁ TEMPORALMENTE
                    String reason = "Simulated payment failure: Insufficient funds or card decline.";
                    Payment failedPayment = savedPayment.markAsFailed(reason);

                    log.warn("❌ Payment FAILED for Order ID {}. Reason: {}", orderId, reason);

                    return persistencePort.update(failedPayment)
                        .then(Mono.defer(() -> {
                            PaymentFailedEvent failureEvent = new PaymentFailedEvent(orderId, reason);
                            return eventPublisherPort.publishPaymentFailedEvent(failureEvent);
                        }));
                }
            })
            .then();
    }

}