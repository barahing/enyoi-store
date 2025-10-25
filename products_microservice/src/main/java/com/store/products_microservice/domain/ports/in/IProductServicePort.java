package com.store.products_microservice.domain.ports.in;

import com.store.products_microservice.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface IProductServicePort {
    Mono<Product> createProduct(Product product);
    Mono<Product> getProductById(UUID id);
    Flux<Product> getAllProducts();
    Mono<Product> updateProduct(UUID id, Product product);
    Mono<Void> deleteProduct(UUID id);
}