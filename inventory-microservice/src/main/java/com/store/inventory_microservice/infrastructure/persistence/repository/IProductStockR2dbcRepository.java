package com.store.inventory_microservice.infrastructure.persistence.repository;

import java.util.UUID;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import com.store.inventory_microservice.infrastructure.persistence.entity.ProductStockEntity;

public interface IProductStockR2dbcRepository extends R2dbcRepository<ProductStockEntity, UUID> {

}
