package com.store.carts_microservice.domain.ports.out;

import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.util.UUID;

public interface IProductServicePort {
    Mono<Boolean> productExists(UUID productId);
    Mono<BigDecimal> getProductPrice(UUID productId);
}