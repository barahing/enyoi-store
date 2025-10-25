package com.store.users_microservice.domain.ports.out;

import java.util.UUID;

import com.store.users_microservice.domain.model.User;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserRepositoryPort { 
    Mono<User> save(User user); 
    Mono<User> findById(UUID id); 
    Flux<User> findAll(); 
    Mono<User> update(UUID id, User user); 
    Mono<Void> deleteById(UUID id); 
}