package com.store.users_microservice.infrastructure.persistence.adapter;

import java.util.UUID;
import org.springframework.stereotype.Component;

import com.store.users_microservice.domain.model.User;
import com.store.users_microservice.domain.ports.out.IUserRepositoryPort;
import com.store.users_microservice.infrastructure.persistence.mapper.UserMapperEntity;
import com.store.users_microservice.infrastructure.persistence.repository.IUserR2dbcRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements IUserRepositoryPort {
    private final IUserR2dbcRepository userRepository;
    private final UserMapperEntity mapper;

    @Override
    public Mono<User> save(User user) {
        return userRepository.save(mapper.toEntity(user)).map(mapper::toDomain);
    }
    
    @Override
    public Mono<User> findById(UUID id) {
        return userRepository.findById(id).map(mapper::toDomain);
    }
    
    @Override
    public Flux<User> findAll() {
        return userRepository.findAll().map(mapper::toDomain);
    }
    
    @Override
    public Mono<User> update(UUID id, User user) {
        return userRepository.save(mapper.toEntity(user)).map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return userRepository.deleteById(id);
    }
}