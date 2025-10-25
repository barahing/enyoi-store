package com.store.products_microservice.infrastructure.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Importamos el nuevo puerto
import com.store.products_microservice.domain.ports.in.IStockManagementPort; 

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    // Inyectamos el nuevo puerto de entrada
    private final IStockManagementPort inventoryUseCases; 

    @PostMapping("/{productId}/increase/{quantity}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> increase (@PathVariable UUID productId, @PathVariable int quantity) {
        return inventoryUseCases.increaseStock(productId, quantity);
    }

    @PostMapping("/{productId}/decrease/{quantity}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> decrease (@PathVariable UUID productId, @PathVariable int quantity) {
        return inventoryUseCases.decreaseStock(productId, quantity);
    }

    @GetMapping("/{productId}/check/{quantity}")
    public Mono<ResponseEntity<Map<String, Object>>> check (@PathVariable UUID productId, @PathVariable int quantity) {
        // Renombramos el método isInStock a checkStockAvailability
        return inventoryUseCases.checkStockAvailability(productId, quantity) 
            .map(inStock -> ResponseEntity.ok(Map.of(
                "productId", productId,
                "available", inStock,
                "requested", quantity
            )));
    }
}