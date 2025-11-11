package com.store.purchases_microservice.infrastructure.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.store.purchases_microservice.domain.model.PurchaseStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PurchaseResponseDTO {
    UUID id;
    String supplierName;
    UUID productId;
    Integer quantity;
    BigDecimal unitCost;
    PurchaseStatus status;
    LocalDateTime orderDate;
    LocalDateTime deliveryDate;
}
