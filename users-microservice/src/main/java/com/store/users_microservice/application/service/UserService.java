package com.store.users_microservice.application.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.store.users_microservice.domain.exception.UserNotFoundException;
import com.store.users_microservice.domain.model.User;
import com.store.users_microservice.domain.ports.in.IUserUseCases;
import com.store.users_microservice.domain.ports.out.IUserPersistencePort;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService implements IUserUseCases {

    private final IUserPersistencePort persistence;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> createUser(User user) {
        String hashedPassword = passwordEncoder.encode(user.passwordHash()); // aquí venía en claro
        User newUser = new User(
            UUID.randomUUID(),
            user.firstName(),
            user.lastName(),
            user.email(),
            hashedPassword
        );
        return persistence.saveUser(newUser);
    }

    @Override
    public Mono<User> getUserById(UUID id) {
        return persistence.findUserById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)));
    }

    @Override
    public Flux<User> getAllUsers() {
        return persistence.findAllUsers();
    }

    @Override
    public Mono<User> updateUser(UUID id, User user) {
        return persistence.findUserById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            .flatMap(existingUser -> {
                String finalPasswordHash = user.passwordHash() != null ? passwordEncoder.encode(user.passwordHash()) : existingUser.passwordHash(); 

                User updatedUser = new User(
                    existingUser.id(),
                    user.firstName() != null ? user.firstName() : existingUser.firstName(),
                    user.lastName()  != null ? user.lastName()  : existingUser.lastName(),
                    user.email()     != null ? user.email()     : existingUser.email(),
                    finalPasswordHash
                );
                return persistence.updateUser(id, updatedUser);
            });
    }


    @Override
    public Mono<Void> deleteUser(UUID id) {
        return persistence.findUserById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                .flatMap(u -> persistence.deleteUser(id));
    }
}
