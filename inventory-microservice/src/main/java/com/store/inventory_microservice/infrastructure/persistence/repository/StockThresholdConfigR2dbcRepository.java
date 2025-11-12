package com.store.inventory_microservice.infrastructure.persistence.repository;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import com.store.inventory_microservice.infrastructure.persistence.entity.StockThresholdConfigEntity;

public interface StockThresholdConfigR2dbcRepository extends ReactiveCrudRepository<StockThresholdConfigEntity, UUID> {
}
