package com.store.orders_microservice.infrastructure.web.dto;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {
    @NotNull(message = "Client ID is required")
    private UUID clientId;
    @NotNull(message = "Items are required")
    private List<OrderItemRequestDto> items;
}
