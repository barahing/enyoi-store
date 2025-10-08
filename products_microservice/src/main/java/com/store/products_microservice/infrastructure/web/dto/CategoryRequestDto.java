package com.store.products_microservice.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryRequestDto {
    @NotBlank @Size(max = 80, message = "Category name must be at most 100 characters") 
    private String name;
    @NotBlank @Size(max = 150, message = "Category description must be at most 150 characters") 
    private String description;
}
