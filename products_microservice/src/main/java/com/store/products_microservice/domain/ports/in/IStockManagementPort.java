package com.store.products_microservice.domain.ports.in;

import com.store.common.events.ReserveStockCommand;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface IStockManagementPort {
    // Para el check síncrono del Cart Service
    Mono<Boolean> checkStockAvailability(UUID productId, int requiredQuantity);

    // Puerto de entrada para el Listener de RabbitMQ (nuestro UseCase principal)
    Mono<Void> handleStockReservation(ReserveStockCommand command);

    // Métodos administrativos
    Mono<Void> increaseStock(UUID productId, int quantity);
    Mono<Void> decreaseStock(UUID productId, int quantity);
}