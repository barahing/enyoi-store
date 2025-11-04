package com.store.products_microservice.application.service;

import java.util.UUID;
import org.springframework.stereotype.Service;

import com.store.products_microservice.domain.exception.ProductNotFoundException;
import com.store.products_microservice.domain.model.Product;
import com.store.products_microservice.domain.ports.in.IProductServicePort;
import com.store.products_microservice.domain.ports.out.IProductRepositoryPort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductServicePort {
    private final IProductRepositoryPort productRepository;
    
    @Override
    public Mono<Product> createProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Mono<Product> getProductById(UUID id) {
        return productRepository.findById(id)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(id)));
    }

    @Override
    public Flux<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Mono<Product> updateProduct(UUID id, Product product) {
        return productRepository.findById(id)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
            .flatMap(existingProduct -> {
                Product updatedProduct = new Product (
                    existingProduct.id(),
                    product.name() != null ? product.name() : existingProduct.name(),
                    product.description() != null ? product.description() : existingProduct.description(),
                    product.price() != null ? product.price() : existingProduct.price(),
                    product.categoryId() != null ? product.categoryId() : existingProduct.categoryId()
                );
                return productRepository.save(updatedProduct);
            });
    }

    @Override
    public Mono<Void> deleteProduct(UUID id) {
        return productRepository.deleteById(id);
    }
}