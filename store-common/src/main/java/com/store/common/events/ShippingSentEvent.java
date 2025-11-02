package com.store.common.events;

import java.util.UUID;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShippingSentEvent {
    
    private UUID orderId;
    private String trackingNumber;
    private String carrier;
    private LocalDateTime sentDate = LocalDateTime.now();
}