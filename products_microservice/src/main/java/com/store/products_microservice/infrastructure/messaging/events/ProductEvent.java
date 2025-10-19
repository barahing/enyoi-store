package com.store.products_microservice.infrastructure.messaging.events;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductEvent {
    private String type;      
    private UUID productId;
    private Integer stock;    
}
