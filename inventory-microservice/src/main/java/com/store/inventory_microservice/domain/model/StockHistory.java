package com.store.inventory_microservice.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockHistory {

    private UUID id;
    private UUID productId;
    private String action;
    private Integer quantity;
    private String reference;    
    private LocalDateTime timestamp;

    public static StockHistory create(UUID productId, String action, int quantity, String reference) {
        return new StockHistory(
            UUID.randomUUID(),
            productId,
            action,
            quantity,
            reference,
            LocalDateTime.now()
        );
    }
}
