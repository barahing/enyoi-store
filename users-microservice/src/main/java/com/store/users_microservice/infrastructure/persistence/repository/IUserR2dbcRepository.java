package com.store.users_microservice.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.store.users_microservice.infrastructure.persistence.entity.UserEntity;

public interface IUserR2dbcRepository extends ReactiveCrudRepository<UserEntity, UUID> {

}
