package main.java.com.store.common.events;

import java.util.UUID;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCreatedEvent {
    private UUID userId;
    private String email;
}
