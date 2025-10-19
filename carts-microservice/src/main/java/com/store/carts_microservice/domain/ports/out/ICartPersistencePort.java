    package com.store.carts_microservice.domain.ports.out;

    import java.util.UUID;

    import com.store.carts_microservice.domain.model.Cart;

    import reactor.core.publisher.Flux;
    import reactor.core.publisher.Mono;

    public interface ICartPersistencePort {
        Mono<Cart> saveCart(Cart cart);
        Mono<Cart> findCartById(UUID id);
        Flux<Cart> findAllCarts();
        Mono<Cart> updateCart(Cart cart);
        Mono<Void> deleteCart(UUID id);
    }
