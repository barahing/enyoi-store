package com.store.notifications_microservice.domain.model;

import java.time.Instant;

public record Notification(
    String recipient,
    String subject,
    String body,
    Instant timestamp
) {}
