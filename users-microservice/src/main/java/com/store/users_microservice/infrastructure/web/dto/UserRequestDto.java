package com.store.users_microservice.infrastructure.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
    @NotBlank @Size(max = 50) 
    private String firstName;
    @NotBlank @Size(max = 50) 
    private String lastName;
    @Email @NotBlank
    private String email;
    @NotBlank @Size(min = 8, max = 100)
    private String password;
}
