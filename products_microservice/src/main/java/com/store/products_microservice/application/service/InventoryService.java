package com.store.products_microservice.application.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.store.products_microservice.domain.exception.ProductNotFoundException;
import com.store.products_microservice.domain.exception.NotEnoughStockException;
import com.store.products_microservice.domain.model.Product;
import com.store.products_microservice.domain.ports.in.IStockManagementPort;
import com.store.products_microservice.domain.ports.out.IProductRepositoryPort;
import com.store.common.events.ReserveStockCommand;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InventoryService implements IStockManagementPort {

    private final IProductRepositoryPort productRepository;

    @Override
    public Mono<Void> increaseStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            return Mono.error(new IllegalArgumentException("Quantity must be positive"));
        }
        return productRepository.findById(productId)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
            .flatMap(product -> {
                Product updated = new Product(
                    product.id(),
                    product.name(),
                    product.description(),
                    product.price(),
                    product.stockAvailable() + quantity,
                    product.stockReserved(),
                    product.categoryId()
                );
                return productRepository.save(updated).then();
            });
    }

    @Override
    public Mono<Void> decreaseStock(UUID productId, int quantity) {
        if (quantity <= 0) {
            return Mono.error(new IllegalArgumentException("Quantity must be positive"));
        }
        return productRepository.findById(productId)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(productId)))
            .flatMap(product -> {
                int newStockAvailable = product.stockAvailable() - quantity;
                
                if (newStockAvailable < 0) {
                    return Mono.error(new NotEnoughStockException("Not enough available stock for administrative decrease."));
                }
                
                Product updated = new Product(
                    product.id(),
                    product.name(),
                    product.description(),
                    product.price(),
                    newStockAvailable,
                    product.stockReserved(),
                    product.categoryId()
                );
                return productRepository.save(updated).then();
            });
    }

    @Override
    public Mono<Boolean> checkStockAvailability(UUID productId, int requiredQuantity) {
        return productRepository.findById(productId)
            .map(product -> product.canReserve(requiredQuantity))
            .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> handleStockReservation(ReserveStockCommand command) {
        return Mono.empty();
    }
}