package com.store.users_microservice.infrastructure.web.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import org.springframework.http.HttpStatus;
import com.store.users_microservice.domain.exception.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleUserNotFound(UserNotFoundException ex) {
        return Map.of("error", "USER_NOT_FOUND", "message", ex.getMessage());
    }
}

