package com.store.users_microservice.infrastructure.web.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.store.users_microservice.domain.ports.in.IUserUseCases;
import com.store.users_microservice.infrastructure.web.dto.UserRequestDto;
import com.store.users_microservice.infrastructure.web.dto.UserResponseDto;
import com.store.users_microservice.infrastructure.web.mapper.UserMapperDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserUseCases userUseCases;
    private final UserMapperDto userMapper;

    @GetMapping()
    public Flux<UserResponseDto> findAll() {
        return userUseCases.getAllUsers()
                .map(userMapper::toResponseDto);
    }

    @GetMapping("/{userId}")
    public Mono<UserResponseDto> findById(@Valid @PathVariable("userId") UUID userId) {
        return userUseCases.getUserById(userId)
                .map(userMapper::toResponseDto);
    }

    @PutMapping("/{userId}")
    public Mono<UserResponseDto> updateUser(@Valid @PathVariable("userId") UUID userId, @RequestBody UserRequestDto user) {
        return userUseCases.updateUser(userId, userMapper.toDomain(user))
                .map(userMapper::toResponseDto);
    }

    @PostMapping()
    public Mono<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto user) {
        return userUseCases.createUser(userMapper.toDomain(user))
                .map(userMapper::toResponseDto);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Map<String, String>>> deleteUser(@Valid @PathVariable("userId") UUID userId) {
        return userUseCases.deleteUser(userId)
                .thenReturn(ResponseEntity.ok(Map.of("status", "deleted", "id", userId.toString())));
    }

}
