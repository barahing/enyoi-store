package com.store.carts_microservice.infrastructure.persistence.adapter;

import com.store.carts_microservice.domain.ports.out.ICartReportPersistencePort;
import com.store.carts_microservice.infrastructure.persistence.entity.CartEntity;
import com.store.carts_microservice.infrastructure.persistence.entity.CartItemEntity;
import com.store.carts_microservice.infrastructure.persistence.repository.ICartItemR2dbcRepository;
import com.store.carts_microservice.infrastructure.persistence.repository.ICartR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartReportR2dbcAdapter implements ICartReportPersistencePort {

    private final ICartR2dbcRepository cartRepo;
    private final ICartItemR2dbcRepository itemRepo;

    @Override
    public Flux<CartEntity> findActiveCartsWithoutOrder() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        return cartRepo.findAll()
                .filter(cart ->
                        "ACTIVE".equalsIgnoreCase(cart.getStatus()) &&
                        cart.getUpdatedDate() != null &&
                        cart.getUpdatedDate().isBefore(oneHourAgo)
                )
                .flatMap(cart ->
                        itemRepo.findAllByCartId(cart.getId())
                                .hasElements()
                                .filter(hasItems -> hasItems)
                                .map(hasItems -> cart)
                )
                .doOnSubscribe(sub -> log.info("🔍 Buscando carritos abandonados (última actualización antes de {})", oneHourAgo))
                .doOnComplete(() -> log.info("✅ Búsqueda de carritos abandonados completada"));
    }

    @Override
    public Flux<CartItemEntity> findItemsByCartId(UUID cartId) {
        return itemRepo.findAllByCartId(cartId);
    }
}
