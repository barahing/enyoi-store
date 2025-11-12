// En: users_microservice/domain/ports/out/IUserEventPublisherPort.java
package com.store.users_microservice.domain.ports.out;

import com.store.common.events.UserCreatedEvent;
import com.store.common.events.UserDeactivatedEvent;
import com.store.common.events.UserActivatedEvent;
import reactor.core.publisher.Mono;

public interface IUserEventPublisherPort {
    Mono<Void> publishUserCreated(UserCreatedEvent event);
    Mono<Void> publishUserDeactivated(UserDeactivatedEvent event);  
    Mono<Void> publishUserActivated(UserActivatedEvent event);      
}