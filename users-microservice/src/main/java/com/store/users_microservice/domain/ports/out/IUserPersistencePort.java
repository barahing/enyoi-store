package com.store.users_microservice.domain.ports.out;

import java.util.UUID;

import com.store.users_microservice.domain.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserPersistencePort {
    Mono<User> saveUser(User user);
    Mono<User> findUserById(UUID id);
    Flux<User> findAllUsers();
    Mono<User> updateUser(UUID id, User user);
    Mono<Void> deleteUser(UUID id);
}
