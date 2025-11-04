package com.store.inventory_microservice.infrastructure.catalog;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.store.inventory_microservice.domain.ports.out.IProductCatalogPort;
import com.store.inventory_microservice.infrastructure.client.dto.ProductDto; 

import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProductCatalogAdapter implements IProductCatalogPort {

    private final WebClient webClient;

    public ProductCatalogAdapter(@Value("${app.product-service.url}") String productBaseUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(productBaseUrl)
            .build();
    }

    @Override
    public Mono<Boolean> productExists(UUID productId) {
        return webClient.get()
            .uri("/api/products/{id}", productId)
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return Mono.just(true);
                } else if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                    log.warn("Product with ID {} not found (404).", productId);
                    return Mono.just(false);
                } else {
                    log.error("Error connecting to Product Service: {}", response.statusCode());
                    return Mono.just(false);
                }
            })
            .onErrorResume(e -> {
                log.error("Unexpected network error when checking product existence: {}", e.getMessage());
                return Mono.just(false);
            });
    }

    @Override
    public Mono<ProductDto> getProductById(UUID productId) {
        return webClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .onStatus(status -> status.equals(HttpStatus.NOT_FOUND),
                          clientResponse -> {
                            // Cuando el producto no existe (404), devuelve Mono.empty()
                            log.warn("Product with ID {} not found (404) during retrieval.", productId);
                            return Mono.empty();
                          })
                .bodyToMono(ProductDto.class)
                .onErrorResume(e -> {
                    // Maneja errores de red o del servidor (e.g., 5xx, timeout)
                    log.error("Unexpected error retrieving Product details for ID {}: {}", productId, e.getMessage());
                    return Mono.empty();
                });
    }
}
