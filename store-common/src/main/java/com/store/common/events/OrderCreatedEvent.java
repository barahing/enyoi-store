package main.java.com.store.common.events;

import java.util.UUID;
import java.math.BigDecimal;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderCreatedEvent {
    private UUID orderId;
    private UUID clientId;
    private BigDecimal total;
}
