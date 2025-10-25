package com.store.common.events;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;

public record UserCreatedEvent(
    @JsonProperty("userId") UUID userId,
    @JsonProperty("email") String email,
    @JsonProperty("firstName") String firstName, 
    @JsonProperty("lastName") String lastName,
    @JsonProperty("role") String role 
) implements Serializable {}