package com.store.common.events;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockReceivedEvent {
    
    private UUID productId;
    private Integer quantityReceived;
    private UUID purchaseOrderId;
}
