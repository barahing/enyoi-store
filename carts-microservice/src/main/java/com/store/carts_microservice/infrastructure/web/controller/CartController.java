package com.store.carts_microservice.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.UUID;

import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final ICartServicePort cartService;

    @PostMapping("/client/{clientId}")
    public Mono<Cart> createCart(@PathVariable UUID clientId) {
        return cartService.createCartForClient(clientId);
    }

    @GetMapping("/client/{clientId}/active")
    public Mono<Cart> getActiveCart(@PathVariable UUID clientId) {
        return cartService.getActiveCartByClientId(clientId);
    }

    @PostMapping("/{cartId}/items")
    public Mono<Cart> addItem(@PathVariable UUID cartId, @RequestBody CartItem item) {
        return cartService.addProductToCart(cartId, item);
    }

    @PutMapping("/{cartId}/convert-to-order")
    public Mono<Cart> convertToOrder(@PathVariable UUID cartId) {
        return cartService.convertCartToOrder(cartId);
    }
}