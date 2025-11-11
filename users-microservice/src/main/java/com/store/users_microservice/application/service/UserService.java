package com.store.users_microservice.application.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.store.common.events.UserActivatedEvent;
import com.store.common.events.UserCreatedEvent;
import com.store.common.events.UserDeactivatedEvent;
import com.store.users_microservice.domain.exception.UserNotFoundException;
import com.store.users_microservice.domain.model.Role;
import com.store.users_microservice.domain.model.User;
import com.store.users_microservice.domain.ports.in.IUserServicePort;
import com.store.users_microservice.domain.ports.out.IUserRepositoryPort;
import com.store.users_microservice.domain.ports.out.IUserEventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserServicePort {

    private final IUserRepositoryPort userRepository; 
    private final IUserEventPublisherPort userEventPublisherPort; 
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<User> createUser(User user) {
        log.info("🧩 [USER SERVICE] Creating user {}", user.email());

        User newUser = new User(
            null,
            user.firstName(),
            user.lastName(),
            user.email(),
            passwordEncoder.encode(user.passwordHash()),
            user.role()
        );

        return userRepository.save(newUser)
            .flatMap(savedUser -> {
                var event = new UserCreatedEvent(
                    savedUser.id(),
                    savedUser.email(),
                    savedUser.firstName(),
                    savedUser.lastName(),
                    savedUser.role().name()
                );

                userEventPublisherPort.publishUserCreated(event)
                    .doOnSubscribe(sub -> log.info("📢 [USERS] Publishing UserCreatedEvent for {}", savedUser.email()))
                    .subscribe();

                return Mono.just(savedUser);
            })
            .cache()
            .doOnSuccess(u -> log.info("✅ [USERS] User {} persisted successfully", u.email()))
            .doOnError(e -> log.error("❌ [USERS] Failed to create user {}: {}", user.email(), e.getMessage()));
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

    @Override
    public Mono<Void> deactivateUser(UUID userId) {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
            .flatMap(user -> {
                User deactivatedUser = new User(
                    user.id(),
                    user.firstName(),
                    user.lastName(), 
                    user.email(),
                    user.passwordHash(),
                    Role.INACTIVE
                );
                
                return userRepository.update(userId, deactivatedUser)
                    .then(userEventPublisherPort.publishUserDeactivated(  
                        new UserDeactivatedEvent(userId)                  
                    ));
            });
    }

    @Override
    public Mono<Void> activateUser(UUID userId) {
        return userRepository.findById(userId)
            .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
            .flatMap(user -> {
                User activatedUser = new User(
                    user.id(),
                    user.firstName(),
                    user.lastName(),
                    user.email(), 
                    user.passwordHash(),
                    Role.CLIENT
                );
                
                return userRepository.update(userId, activatedUser)
                    .then(userEventPublisherPort.publishUserActivated(     
                        new UserActivatedEvent(userId)                     
                    ));
            });
    }

    @Override
    public Mono<Boolean> userCanBeDeleted(UUID userId) {
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> userHasPendingOrders(UUID userId) {
        return Mono.just(false);
}
}