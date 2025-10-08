package com.store.products_microservice.domain.ports.in;

import java.util.UUID;

import com.store.products_microservice.domain.model.Product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductUseCases {
    Mono<Product> createProduct(Product product);
    Mono<Product> getProductById(UUID id);
    Flux<Product> getAllProduct();
    Mono<Product> updateProduct(UUID id, Product product);
    Mono<Void> deleteProduct (UUID id);
}
