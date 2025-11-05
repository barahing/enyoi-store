package com.store.carts_microservice.infrastructure.adapters.http;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.UUID;

import com.store.carts_microservice.domain.ports.out.IUserServicePort;

@Component
@RequiredArgsConstructor
public class UserServiceAdapter implements IUserServicePort {
    
    private final WebClient.Builder webClientBuilder;
    
    @Override
    public Mono<Boolean> isClientActive(UUID clientId) {
        return webClientBuilder.build()
            .get()
            .uri("http://user-service/api/users/{clientId}/active", clientId)
            .retrieve()
            .bodyToMono(Boolean.class)
            .onErrorReturn(false);
    }
    
    @Override
    public Mono<String> getClientRole(UUID clientId) {
        return webClientBuilder.build()
            .get()
            .uri("http://user-service/api/users/{clientId}/role", clientId)
            .retrieve()
            .bodyToMono(String.class)
            .onErrorReturn("INACTIVE"); // Si falla, asumimos que no está activo
    }
}