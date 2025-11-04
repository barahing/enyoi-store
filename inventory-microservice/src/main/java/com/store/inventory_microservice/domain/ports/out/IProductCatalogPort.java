package com.store.inventory_microservice.domain.ports.out;

import java.util.UUID;

import com.store.inventory_microservice.infrastructure.client.dto.ProductDto;

import reactor.core.publisher.Mono;

public interface IProductCatalogPort {

    Mono<Boolean> productExists(UUID productId);

    Mono<ProductDto> getProductById(UUID productId);
}
