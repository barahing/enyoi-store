package com.store.orders_microservice.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemRequestDto {

    @NotNull(message = "Product ID is required")
    private UUID productId;
    @NotNull
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;
    @NotNull
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;
}
