package main.java.com.store.common.events;

import java.util.UUID;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartConfirmedEvent {
    private UUID cartId;
    private UUID clientId;
}
