package com.store.users_microservice.domain.ports.in;

import java.util.UUID;

import com.store.users_microservice.domain.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserServicePort {
    Mono<User> createUser(User user);
    Mono<User> getUserById(UUID id);
    Flux<User> getAllUsers();
    Mono<User> updateUser(UUID id, User user);
    Mono<Void> deleteUser(UUID id);
    Mono<Void> deactivateUser(UUID userId);
    Mono<Void> activateUser(UUID userId);
    Mono<Boolean> userCanBeDeleted(UUID userId);
    Mono<Boolean> userHasPendingOrders(UUID userId);
}