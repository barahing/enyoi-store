package com.store.users_microservice.application.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.store.common.events.UserCreatedEvent;
import com.store.users_microservice.domain.exception.UserNotFoundException;
import com.store.users_microservice.domain.model.User;
import com.store.users_microservice.domain.ports.in.IUserServicePort;
import com.store.users_microservice.domain.ports.out.IUserRepositoryPort;
import com.store.users_microservice.domain.ports.out.IUserEventPublisherPort;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService implements IUserServicePort {

    private final IUserRepositoryPort userRepository; 
    private final IUserEventPublisherPort userEventPublisherPort; 
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> createUser(User user) {
        String hashedPassword = passwordEncoder.encode(user.passwordHash());
        
        User newUserWithHash = new User(
            null,
            user.firstName(),
            user.lastName(),
            user.email(),
            hashedPassword,
            user.role() 
        );
        
        return userRepository.save(newUserWithHash)
            .flatMap(savedUser -> {
                UserCreatedEvent event = new UserCreatedEvent(
                    savedUser.id(),
                    savedUser.email(),
                    savedUser.firstName(),
                    savedUser.lastName(),
                    savedUser.role().name() 
                );
                
                return userEventPublisherPort.publishUserCreated(event)
                    .thenReturn(savedUser); 
            });
    }

    @Override
    public Mono<User> getUserById(UUID id) {
        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)));
    }

    @Override
    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Mono<User> updateUser(UUID id, User user) {
        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            .flatMap(existingUser -> {
                String finalPasswordHash = user.passwordHash() != null ? passwordEncoder.encode(user.passwordHash()) : existingUser.passwordHash(); 

                User updatedUser = new User(
                    existingUser.id(),
                    user.firstName() != null ? user.firstName() : existingUser.firstName(),
                    user.lastName()  != null ? user.lastName()  : existingUser.lastName(),
                    user.email()     != null ? user.email()     : existingUser.email(),
                    finalPasswordHash,
                    existingUser.role() 
                );
                
                return userRepository.update(id, updatedUser);
            });
    }


    @Override
    public Mono<Void> deleteUser(UUID id) {
        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            .flatMap(u -> userRepository.deleteById(id));
    }
}