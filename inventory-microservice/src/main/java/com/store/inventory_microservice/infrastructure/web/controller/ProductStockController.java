package com.store.inventory_microservice.infrastructure.web.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.store.inventory_microservice.domain.ports.in.IProductStockServicePort;
import com.store.inventory_microservice.infrastructure.web.dto.InitialStockRequestDto;
import com.store.inventory_microservice.infrastructure.web.dto.ProductStockResponseDto;
import com.store.inventory_microservice.infrastructure.web.mapper.IProductStockMapperDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@Validated
public class ProductStockController {
    
    private final IProductStockServicePort stockServicePort;
    private final IProductStockMapperDto stockMapper;

    @GetMapping("/{productId}")
    public Mono<ProductStockResponseDto> getStockByProductId(@PathVariable UUID productId) {
        return stockServicePort.getStockByProductId(productId)
                .map(stockMapper::toDto);
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ProductStockResponseDto> createInitialStock(@RequestBody @Validated InitialStockRequestDto requestDto) {
        return stockServicePort.createInitialStock(requestDto.getProductId(), requestDto.getInitialStock())
                .map(stockMapper::toDto);
    }
}