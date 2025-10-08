package com.store.products_microservice.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.store.products_microservice.domain.exception.ProductNotFoundException;
import com.store.products_microservice.domain.model.Product;
import com.store.products_microservice.domain.ports.in.IProductUseCases;
import com.store.products_microservice.domain.ports.out.IProductPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductUseCases{
    private final IProductPersistencePort persistence; 
    
    @Override
    public Mono<Product> createProduct(Product product) {
        return persistence.saveProduct(product);
    }

    @Override
    public Mono<Product> getProductById(UUID id) {
        return persistence.findProductById(id)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }

    @Override
    public Flux<Product> getAllProduct() {
        return persistence.findAllProducts();
    }

    @Override
    public Mono<Product> updateProduct(UUID id, Product product) {
        return persistence.findProductById(id)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
            .flatMap(existingProduct -> {
                Product updatedProduct = new Product (
                    existingProduct.id(),
                    product.name() != null ? product.name() : existingProduct.name(),
                    product.description() != null ? product.description() : existingProduct.description(),
                    product.price() != null ? product.price() : existingProduct.price(),
                    product.stock() != null ? product.stock() : existingProduct.stock(),
                    product.categoryId() != null ? product.categoryId() : existingProduct.categoryId()
                );
                return persistence.updateProduct(id, updatedProduct);
            });
    }

    @Override
    public Mono<Void> deleteProduct(UUID id) {
        return persistence.deleteProduct(id)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
            .flatMap(u -> persistence.deleteProduct(id));
    }

}
