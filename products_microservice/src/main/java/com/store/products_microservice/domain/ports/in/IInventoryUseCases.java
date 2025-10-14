package com.store.products_microservice.domain.ports.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface IInventoryUseCases {
    Mono<Void> increaseStock(UUID productId, int quantity);
    Mono<Void> decreaseStock(UUID productId, int quantity);
    Mono<Boolean> isInStock(UUID productId, int requiredQuantity);
}
