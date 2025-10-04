package com.store.users_microservice.infrastructure.persistence.adapter;

import java.util.UUID;
import org.springframework.stereotype.Component;

import com.store.users_microservice.domain.model.User;
import com.store.users_microservice.domain.ports.out.IUserPersistencePort;
import com.store.users_microservice.infrastructure.persistence.mapper.UserMapperEntity;
import com.store.users_microservice.infrastructure.persistence.repository.IUserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements IUserPersistencePort {
    private final IUserR2dbcRepository userRepository;
    private final UserMapperEntity mapper;

    @Override
    public Mono<User> saveUser(User user) {
        return userRepository.save(mapper.toEntity(user)).map(mapper::toDomain);
    }
    @Override
    public Mono<User> findUserById(UUID id) {
        return userRepository.findById(id).map(mapper::toDomain);
    }
    @Override
    public Flux<User> findAllUsers() {
        return userRepository.findAll().map(mapper::toDomain);
    }
    @Override
    public Mono<User> updateUser(UUID id, User user) {
        return userRepository.save(mapper.toEntity(user)).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteUser(UUID id) {
        return userRepository.deleteById(id);
    }
}
