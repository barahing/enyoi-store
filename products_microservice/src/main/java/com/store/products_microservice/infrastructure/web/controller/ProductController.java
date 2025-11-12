package com.store.products_microservice.infrastructure.web.controller;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import com.store.products_microservice.domain.ports.in.IProductServicePort; 
import com.store.products_microservice.infrastructure.web.dto.ProductRequestDto;
import com.store.products_microservice.infrastructure.web.dto.ProductResponseDto;
import com.store.products_microservice.infrastructure.web.mapper.ProductMapperDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final IProductServicePort productUseCases; 
    private final ProductMapperDto productMapper;

    @GetMapping()
    public Flux<ProductResponseDto> findAll() {
        return productUseCases.getAllProducts()
                .map(productMapper::toResponseDto);
    }

    @GetMapping("/{productId}")
    public Mono<ProductResponseDto> findById(@Valid @PathVariable("productId") UUID productId) {
        return productUseCases.getProductById(productId)
                .map(productMapper::toResponseDto);
    }

    @PutMapping("/{productId}")
    public Mono<ProductResponseDto> updateProduct(@Valid @PathVariable("productId") UUID productId, @RequestBody ProductRequestDto product) {
        return productUseCases.updateProduct(productId, productMapper.toDomain(product))
                .map(productMapper::toResponseDto);
    }

    @PostMapping()
    public Mono<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto product) {
        return productUseCases.createProduct(productMapper.toDomain(product)) 
                .map(productMapper::toResponseDto);
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Map<String, String>>> deleteProduct(@Valid @PathVariable("productId") UUID productId) {
        return productUseCases.deleteProduct(productId)
                .thenReturn(ResponseEntity.ok(Map.of("status", "deleted", "id", productId.toString())));
    }
}
