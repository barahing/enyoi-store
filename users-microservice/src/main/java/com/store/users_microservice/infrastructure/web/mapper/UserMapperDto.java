package com.store.users_microservice.infrastructure.web.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.store.users_microservice.domain.model.User;
import com.store.users_microservice.infrastructure.web.dto.UserRequestDto;
import com.store.users_microservice.infrastructure.web.dto.UserResponseDto;

@Mapper(componentModel = "spring")
public interface UserMapperDto {
    UserResponseDto toResponseDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", source = "password") 
    User toDomain(UserRequestDto requestDto);
}

