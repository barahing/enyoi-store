package com.store.users_microservice.infrastructure.web.dto;

import com.store.users_microservice.domain.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true) 
public class UserAdminRequestDto extends UserRequestDto { 
    
    @NotNull(message = "Role is required for administrative user creation")
    private Role role;

    public UserAdminRequestDto(
        String firstName,
        String lastName,
        String email,
        String password,
        Role role
    ) {
        super(firstName, lastName, email, password);
        this.role = role;
    }
}