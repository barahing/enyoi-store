package com.store.payments_microservice.domain.model;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class Payment {

    private UUID id; 
    private UUID orderId; 
    private BigDecimal amount; 
    private PaymentStatus status; 
    private String paymentMethod; 
    private String transactionRef; 
    private LocalDateTime createdAt;
    private LocalDateTime processedAt; 

    public static Payment createNew(UUID orderId, BigDecimal amount, String paymentMethod) {
        return Payment.builder()
            .id(UUID.randomUUID())
            .orderId(orderId)
            .amount(amount)
            .paymentMethod(paymentMethod)
            .status(PaymentStatus.PENDING) 
            .createdAt(LocalDateTime.now())
            .build();
    }
    
    public Payment markAsProcessed(String transactionRef) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment is not in PENDING state.");
        }
        return this.toBuilder() 
            .status(PaymentStatus.PROCESSED)
            .transactionRef(transactionRef)
            .processedAt(LocalDateTime.now())
            .build();
    }
    
    public Payment markAsFailed(String reason) {
        return this.toBuilder()
            .status(PaymentStatus.FAILED)
            .processedAt(LocalDateTime.now())
            .build();
    }
}