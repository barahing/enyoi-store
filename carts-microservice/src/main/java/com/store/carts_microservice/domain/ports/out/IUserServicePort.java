package com.store.carts_microservice.domain.ports.out;

import reactor.core.publisher.Mono;
import java.util.UUID;

public interface IUserServicePort {
    Mono<Boolean> isClientActive(UUID clientId);
    Mono<String> getClientRole(UUID clientId); 
}