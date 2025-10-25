package com.store.products_microservice.infrastructure.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequestDto {
    @NotBlank @Size(max = 80, message = "Product name must be at most 100 characters") 
    private String name;
    @NotBlank @Size(max = 150, message = "Description must be at most 255 characters") 
    private String description;
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Price must have up to 10 integer digits and 2 decimals")
    private BigDecimal price;    
    @NotNull(message = "Initial Stock is required")
    @PositiveOrZero(message = "Initial Stock cannot be negative")
    private Integer initialStock;
    @NotNull(message = "Category ID is required")
    private UUID categoryId;
}
