package com.store.carts_microservice.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemEntity {
    @Id
    private UUID id;
    private UUID cartId;
    private UUID productId;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
}
