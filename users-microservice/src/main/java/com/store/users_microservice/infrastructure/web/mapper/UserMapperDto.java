package com.store.users_microservice.infrastructure.web.mapper;

import org.mapstruct.Mapper;

import com.store.users_microservice.domain.factory.UserFactory;
import com.store.users_microservice.domain.model.User;
import com.store.users_microservice.infrastructure.web.dto.UserAdminRequestDto;
import com.store.users_microservice.infrastructure.web.dto.UserRequestDto;
import com.store.users_microservice.infrastructure.web.dto.UserResponseDto;

@Mapper(componentModel = "spring")
public abstract class UserMapperDto {
    
    public abstract UserResponseDto toResponseDto(User user);

    public User toDomain(UserRequestDto requestDto) {
        return UserFactory.createClient(
            requestDto.getFirstName(),
            requestDto.getLastName(),
            requestDto.getEmail(),
            requestDto.getPassword()
        );
    }
    
    public User toDomain(UserAdminRequestDto requestDto) {
        return UserFactory.createNew(
            requestDto.getFirstName(),
            requestDto.getLastName(),
            requestDto.getEmail(),
            requestDto.getPassword(),
            requestDto.getRole()
        );
    }
}