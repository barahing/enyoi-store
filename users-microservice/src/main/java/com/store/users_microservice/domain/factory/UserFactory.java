package com.store.users_microservice.domain.factory;

import java.util.UUID;

import com.store.users_microservice.domain.model.User;

public class UserFactory {
    private UserFactory() {}

    public static User createNew(String firstName, String lastName, String email, String passwordHash) {
        return new User(
            UUID.randomUUID(),
            firstName,
            lastName,
            email,
            passwordHash
        );
    }
}