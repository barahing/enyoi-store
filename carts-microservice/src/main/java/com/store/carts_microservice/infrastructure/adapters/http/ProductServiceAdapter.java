package com.store.carts_microservice.infrastructure.adapters.http;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.util.UUID;

import com.store.carts_microservice.domain.ports.out.IProductServicePort;

@Component
@RequiredArgsConstructor
public class ProductServiceAdapter implements IProductServicePort {
    
    private final WebClient.Builder webClientBuilder;
    
    @Override
    public Mono<Boolean> productExists(UUID productId) {
        return webClientBuilder.build()
            .get()
            .uri("http://product-service/api/products/{productId}/exists", productId)
            .retrieve()
            .bodyToMono(Boolean.class)
            .onErrorReturn(false);
    }
    
    @Override
    public Mono<BigDecimal> getProductPrice(UUID productId) {
        return webClientBuilder.build()
            .get()
            .uri("http://product-service/api/products/{productId}/price", productId)
            .retrieve()
            .bodyToMono(BigDecimal.class)
            .onErrorReturn(BigDecimal.ZERO);
    }
}