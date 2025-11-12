package com.store.inventory_microservice.application.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.store.inventory_microservice.infrastructure.persistence.entity.StockThresholdConfigEntity;
import com.store.inventory_microservice.infrastructure.persistence.repository.StockThresholdConfigR2dbcRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockThresholdConfigService {

    private final StockThresholdConfigR2dbcRepository repository;

    public Mono<Integer> getThresholdValue() {
        return repository.findAll()
            .next()
            .map(StockThresholdConfigEntity::getThresholdValue)
            .switchIfEmpty(Mono.just(10));
    }

    public Mono<StockThresholdConfigEntity> getThresholdEntity() {
        return repository.findAll()
            .next()
            .switchIfEmpty(
                repository.save(
                    new StockThresholdConfigEntity(null, 10, LocalDateTime.now())
                ).doOnSuccess(cfg ->
                    log.info("🆕 [CONFIG] Creada configuración inicial de umbral con valor {}", cfg.getThresholdValue())
                )
            );
    }

    public Mono<StockThresholdConfigEntity> updateThreshold(Integer newValue) {
        return repository.findAll()
            .next()
            .flatMap(existing -> {
                existing.setThresholdValue(newValue);
                existing.setUpdatedAt(LocalDateTime.now());
                log.info("🔄 [CONFIG] Actualizando umbral existente a {}", newValue);
                return repository.save(existing);
            })
            .switchIfEmpty(
                repository.save(
                    new StockThresholdConfigEntity(null, newValue, LocalDateTime.now())
                ).doOnSuccess(cfg ->
                    log.info("🆕 [CONFIG] Creado nuevo registro de umbral con valor {}", newValue)
                )
            );
    }
}
