package com.store.payments_microservice.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("payment")
public class PaymentEntity {

    @Id 
    private UUID id;
    
    private UUID orderId;
    private BigDecimal amount;
    private String status; 
    private String paymentMethod;
    private String transactionRef;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}