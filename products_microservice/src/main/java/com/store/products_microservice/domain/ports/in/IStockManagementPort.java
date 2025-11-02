package com.store.products_microservice.domain.ports.in;

import com.store.common.events.ReserveStockCommand;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface IStockManagementPort {
    Mono<Boolean> checkStockAvailability(UUID productId, int requiredQuantity);
    Mono<Void> handleStockReservation(ReserveStockCommand command);
    Mono<Void> increaseStock(UUID productId, int quantity);
    Mono<Void> decreaseStock(UUID productId, int quantity);
}