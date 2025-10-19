package com.store.carts_microservice.infrastructure.messaging.cache;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class CartProductCache {
    private final Map<UUID, Integer> stock = new ConcurrentHashMap<>();

    public void put(UUID productId, Integer qty) { stock.put(productId, qty); }
    public boolean available(UUID productId, int qty) {
        return stock.containsKey(productId) && stock.get(productId) >= qty;
    }
}
