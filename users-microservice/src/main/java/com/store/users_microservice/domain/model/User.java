package com.store.users_microservice.domain.model;

import java.util.UUID;

public record User (
	UUID id,
	String firstName,
	String lastName,
	String email,
	String passwordHash
){
}

