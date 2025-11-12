package com.store.inventory_microservice.infrastructure.web.controller;

import org.springframework.web.bind.annotation.*;
import com.store.inventory_microservice.application.service.StockThresholdConfigService;
import com.store.inventory_microservice.infrastructure.web.dto.StockThresholdConfigDto;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/inventory/config")
@RequiredArgsConstructor
public class StockThresholdConfigController {

    private final StockThresholdConfigService configService;

    @GetMapping("/threshold")
    public Mono<StockThresholdConfigDto> getThreshold() {
        return configService.getThresholdEntity()
            .map(entity -> new StockThresholdConfigDto(
                entity.getThresholdValue(),
                entity.getUpdatedAt()
            ));
    }

    @PutMapping("/threshold/{value}")
    public Mono<StockThresholdConfigDto> updateThreshold(@PathVariable("value") Integer value) {
        return configService.updateThreshold(value)
            .map(entity -> new StockThresholdConfigDto(
                entity.getThresholdValue(),
                entity.getUpdatedAt()
            ));
    }
}
