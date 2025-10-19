package com.store.carts_microservice.infrastructure.messaging.cache;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class CartUserCache {
    private final Set<UUID> users = ConcurrentHashMap.newKeySet();

    public void add(UUID userId) { users.add(userId); }
    public void remove(UUID userId) { users.remove(userId); }
    public boolean exists(UUID userId) { return users.contains(userId); }
}
