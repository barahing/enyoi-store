package com.store.payments_microservice.application.service;

import java.util.UUID;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import com.store.payments_microservice.domain.model.Payment;
import com.store.payments_microservice.domain.ports.in.IPaymentServicePort;
import com.store.payments_microservice.domain.ports.out.IEventPublisherPort;
import com.store.payments_microservice.domain.ports.out.IPaymentRepositoryPort;
import com.store.payments_microservice.infrastructure.client.OrdersClient;
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
    private final OrdersClient ordersClient;

    @Override
    public Mono<Void> processOrderPayment(ProcessPaymentCommand command) {
        UUID orderId = command.orderId();
        BigDecimal requestedAmount = command.amount();

        log.info("💳 [PAYMENTS] Processing payment for Order ID: {} | Amount: {}", orderId, requestedAmount);

        return ordersClient.getOrderById(orderId.toString())
            .flatMap(order -> {
                if (!"STOCK_RESERVED".equalsIgnoreCase(order.getStatus())) {
                    String reason = String.format(
                        "Order %s not payable: current status = %s (expected STOCK_RESERVED).",
                        orderId, order.getStatus()
                    );
                    log.warn("🚫 [PAYMENTS] {}", reason);
                    return publishFailure(orderId, reason);
                }

                if (order.getTotal().compareTo(requestedAmount) != 0) {
                    String reason = String.format(
                        "Payment amount mismatch for order %s. Expected %s, got %s.",
                        orderId, order.getTotal(), requestedAmount
                    );
                    log.warn("🚫 [PAYMENTS] {}", reason);
                    return publishFailure(orderId, reason);
                }

                Payment payment = Payment.createNew(orderId, requestedAmount, command.paymentMethod());
                String txRef = UUID.randomUUID().toString();
                Payment processed = payment.markAsProcessed(txRef);

                log.info("✅ [PAYMENTS] Payment SUCCESS for Order {} | Tx: {}", orderId, txRef);

                return persistencePort.create(processed)
                    .flatMap(saved -> publishSuccess(orderId, processed, txRef));
            })
            .onErrorResume(ex -> {
                log.error("❌ [PAYMENTS] Error validating or processing payment for order {}: {}", orderId, ex.getMessage());
                return publishFailure(orderId, "Error validating order or processing payment");
            });
    }

    private Mono<Void> publishSuccess(UUID orderId, Payment payment, String txRef) {
        PaymentProcessedEvent event = new PaymentProcessedEvent(orderId, payment.getId(), txRef);
        log.info("📤 [PAYMENTS] Publishing PaymentProcessedEvent for orderId={}", orderId);
        return eventPublisherPort.publishPaymentProcessedEvent(event);
    }

    private Mono<Void> publishFailure(UUID orderId, String reason) {
        PaymentFailedEvent event = new PaymentFailedEvent(orderId, reason);
        log.warn("📤 [PAYMENTS] Publishing PaymentFailedEvent for orderId={} reason={}", orderId, reason);
        return eventPublisherPort.publishPaymentFailedEvent(event);
    }

}



