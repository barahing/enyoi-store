package com.store.orders_microservice.infrastructure.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import com.store.orders_microservice.domain.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table("orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderEntity {
    @Id
    @Column("id")
    private UUID id;
    private UUID clientId;
    private BigDecimal total;
    @Column("status")
    private String status;
    @Column("created_date")
    private LocalDateTime createdDate;
    @Column("updated_date")
    private LocalDateTime updatedDate;
}
