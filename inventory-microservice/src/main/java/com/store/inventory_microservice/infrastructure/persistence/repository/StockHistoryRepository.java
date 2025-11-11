package com.store.inventory_microservice.infrastructure.persistence.repository;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import com.store.inventory_microservice.domain.model.StockHistory;
import reactor.core.publisher.Flux;

@Repository
public interface StockHistoryRepository extends ReactiveCrudRepository<StockHistory, UUID> {
    Flux<StockHistory> findByProductId(UUID productId);
}
