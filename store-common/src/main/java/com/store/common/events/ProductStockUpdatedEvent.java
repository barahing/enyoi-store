package main.java.com.store.common.events;

import java.util.UUID;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductStockUpdatedEvent {
    private UUID productId;
    private int stock;
}
