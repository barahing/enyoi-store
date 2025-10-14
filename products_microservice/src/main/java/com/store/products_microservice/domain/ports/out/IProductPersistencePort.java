package com.store.products_microservice.domain.ports.out;

import java.util.UUID;
import com.store.products_microservice.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IProductPersistencePort {
    Mono<Product> saveProduct(Product product);
    Mono<Product> findProductById(UUID id);
    Flux<Product> findAllProducts();
    Mono<Product> updateProduct(UUID id, Product product);
    Mono<Void> deleteProduct(UUID id);
}
