package com.store.carts_microservice.infrastructure.adapters.http;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.UUID;

import com.store.carts_microservice.domain.ports.out.IInventoryServicePort;

@Component
@RequiredArgsConstructor
public class InventoryServiceAdapter implements IInventoryServicePort {
    
    private final WebClient.Builder webClientBuilder;
    private final String inventoryServiceUrl = "http://inventory-microservice/api/stock";

    @Override
    public Mono<Boolean> isQuantityAvailable(UUID productId, int quantity) {
        return webClientBuilder.build()
            .get()
            .uri(inventoryServiceUrl+"/{productId}/available/{quantity}", 
                 productId, quantity)
            .retrieve()
            .bodyToMono(Boolean.class)
            .onErrorReturn(false);
    }
    
    @Override
    public Mono<Void> reserveStock(UUID productId, int quantity) {
        return Mono.empty();
    }
    
    @Override
    public Mono<Void> releaseStockReservation(UUID productId, int quantity) {
        return webClientBuilder.build()
            .post()
            .uri(inventoryServiceUrl+"/{productId}/release/{quantity}", 
                 productId, quantity)
            .retrieve()
            .bodyToMono(Void.class)
            .onErrorResume(e -> Mono.empty()); // Silenciar errores de rollback
    }

}