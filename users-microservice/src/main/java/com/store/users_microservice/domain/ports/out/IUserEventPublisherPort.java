package com.store.users_microservice.domain.ports.out;

import com.store.common.events.UserCreatedEvent;
import reactor.core.publisher.Mono;

public interface IUserEventPublisherPort {
    Mono<Void> publishUserCreated(UserCreatedEvent event);
}