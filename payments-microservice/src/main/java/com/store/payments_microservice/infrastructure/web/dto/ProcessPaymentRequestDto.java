package com.store.payments_microservice.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessPaymentRequestDto {
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
    private String paymentMethod;
}