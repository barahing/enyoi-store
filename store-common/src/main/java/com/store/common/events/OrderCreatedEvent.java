package com.store.common.events;

import com.store.common.dto.ProductStockDTO; 
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    
    private UUID orderId;
    private UUID userId; 
    private BigDecimal amount;
 
    private List<ProductStockDTO> products; 
}