package com.store.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserActivatedEvent(
    UUID userId,
    LocalDateTime timestamp
) {
    public UserActivatedEvent(UUID userId) {
        this(userId, LocalDateTime.now());
    }
}