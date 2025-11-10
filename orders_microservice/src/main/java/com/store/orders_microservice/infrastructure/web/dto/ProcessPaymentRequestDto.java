// orders/infrastructure/adapters/in/web/dto/ProcessPaymentRequest.java
package com.store.orders_microservice.infrastructure.web.dto;

import lombok.Data;

@Data
public class ProcessPaymentRequestDto {
    private String paymentMethod;
}