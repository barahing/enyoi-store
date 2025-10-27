package com.store.inventory_microservice.domain.ports.out;

import java.util.UUID;
import com.store.inventory_microservice.domain.model.ProductStock;
import reactor.core.publisher.Mono;

public interface IProductStockRepositoryPort {
    
    Mono<ProductStock> findByProductId(UUID productId);
    Mono<ProductStock> save(ProductStock stock); 
}