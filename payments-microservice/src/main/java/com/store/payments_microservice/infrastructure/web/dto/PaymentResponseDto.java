package com.store.payments_microservice.infrastructure.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PaymentResponseDto {
    private String status;
    private String message;
    private UUID orderId;
    private LocalDateTime timestamp;
}
