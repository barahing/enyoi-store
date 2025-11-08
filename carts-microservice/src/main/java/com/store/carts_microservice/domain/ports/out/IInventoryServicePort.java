package com.store.carts_microservice.domain.ports.out;

import reactor.core.publisher.Mono;
import java.util.UUID;

public interface IInventoryServicePort {
    Mono<Boolean> isQuantityAvailable(UUID productId, int quantity);
    Mono<Void> reserveStock(UUID productId, int quantity);
    Mono<Void> releaseStockReservation(UUID productId, int quantity);
}