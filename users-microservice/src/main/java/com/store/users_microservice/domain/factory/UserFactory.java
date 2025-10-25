package com.store.users_microservice.domain.factory;

import com.store.users_microservice.domain.model.Role;
import com.store.users_microservice.domain.model.User;

public class UserFactory {
    private UserFactory() {}

    public static User createNew(String firstName, String lastName, String email, String passwordHash, Role role) {
        return new User(
            null,
            firstName,
            lastName,
            email,
            passwordHash,
            role
        );
    }

    public static User createClient(String firstName, String lastName, String email, String passwordHash) {
        return new User(
            null,
            firstName,
            lastName,
            email,
            passwordHash,
            Role.CLIENT
        );
    }
}