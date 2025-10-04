package com.store.users_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import com.store.users_microservice.domain.model.User;
import com.store.users_microservice.infrastructure.persistence.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapperEntity {
    User toDomain(UserEntity entity);
    UserEntity toEntity(User domain);
}
