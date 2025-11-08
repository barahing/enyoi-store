package com.store.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDeactivatedEvent(
    UUID userId,
    LocalDateTime timestamp
) {
    public UserDeactivatedEvent(UUID userId) {
        this(userId, LocalDateTime.now());
    }
}