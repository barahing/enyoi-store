package com.store.users_microservice.infrastructure.persistence.entity;

import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Table("users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
}