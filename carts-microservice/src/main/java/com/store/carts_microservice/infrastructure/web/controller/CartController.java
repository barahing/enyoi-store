package com.store.carts_microservice.infrastructure.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.UUID;

import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.ports.in.ICartServicePort;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final ICartServicePort cartService;

    @PostMapping("/client/{clientId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Cart> createCart(@PathVariable("clientId") UUID clientId) {
        return cartService.createCartForClient(clientId);
    }

    @GetMapping("/client/{clientId}/active")
    public Mono<Cart> getActiveCart(@PathVariable("clientId") UUID clientId) {
        return cartService.getActiveCartByClientId(clientId);
    }

    @GetMapping("/{cartId}")
    public Mono<Cart> getCart(@PathVariable("cartId") UUID cartId) {
        return cartService.findById(cartId); 
    }

    @GetMapping("/client/{clientId}")
    public Mono<Cart> getCartByClientId(@PathVariable("clientId") UUID clientId) {
    return cartService.getActiveCartByClientId(clientId);
    }

    @PostMapping("/{cartId}/items")
    public Mono<Cart> addItem(@PathVariable("cartId") UUID cartId, @RequestBody CartItem item) {
        return cartService.addProductToCart(cartId, item);
    }

    @PutMapping("/{cartId}/items/{productId}")
    public Mono<Cart> updateItemQuantity(
            @PathVariable UUID cartId, 
            @PathVariable UUID productId, 
            @RequestParam int quantity) {
        return cartService.updateItemQuantity(cartId, productId, quantity);
    }

    @DeleteMapping("/{cartId}/items/{productId}")
    public Mono<Cart> removeItem(@PathVariable("cartId") UUID cartId, @PathVariable("productId") UUID productId) {
        return cartService.removeProductFromCart(cartId, productId);
    }

    @DeleteMapping("/{cartId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCart(@PathVariable("cartId") UUID cartId) {
        return cartService.deleteCart(cartId);
    }

   @PostMapping("/{cartId}/convert-to-order")
    public Mono<ResponseEntity<Cart>> convertToOrder(@PathVariable("cartId") UUID cartId) {
        log.info("🧩 Received convert-to-order request for cartId='{}'", cartId);
        return cartService.convertCartToOrder(cartId)
            .map(ResponseEntity::ok)
            .onErrorResume(e -> {
                log.error("❌ Error converting cart {}: {}", cartId, e.getMessage());
                return Mono.just(ResponseEntity.internalServerError().build());
            });
    }
}