package com.store.products_microservice.domain.ports.out;

import com.store.products_microservice.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;
import java.util.List;

public interface IProductRepositoryPort {
    Mono<Product> save(Product product);
    Mono<Product> findById(UUID id);
    Flux<Product> findAll();
    Mono<Product> update(UUID id, Product product);
    Mono<Void> deleteById(UUID id);

    // Nuevo: Para la Saga, permite buscar y guardar múltiples productos
    Flux<Product> findByIds(List<UUID> ids); 
    Flux<Product> saveAll(Flux<Product> products); 
}