package com.store.purchases_microservice.infrastructure.web.controller;

import com.store.purchases_microservice.domain.ports.in.IPurchaseServicePorts;
import com.store.purchases_microservice.infrastructure.web.dto.PurchaseRequestDTO;
import com.store.purchases_microservice.infrastructure.web.dto.PurchaseResponseDTO;
import com.store.purchases_microservice.infrastructure.web.mapper.PurchaseDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final IPurchaseServicePorts purchaseService;
    private final PurchaseDtoMapper mapper;

    @PostMapping
    public Mono<PurchaseResponseDTO> createPurchaseOrder(@Valid @RequestBody PurchaseRequestDTO request) {
        log.info("🧾 [PURCHASES] Creating new purchase for productId={} qty={}",
                request.getProductId(), request.getQuantity());

        return purchaseService.createPurchaseOrder(
                    request.getSupplierName(),
                    request.getProductId(),
                    request.getQuantity(),
                    request.getUnitCost()
                )
                .map(mapper::toResponseDto);
    }

    @PutMapping("/{purchaseOrderId}/receive")
    public Mono<PurchaseResponseDTO> markAsReceived(@PathVariable("purchaseOrderId") UUID purchaseOrderId) {
        log.info("📦 [PURCHASES] Receiving purchase order {}", purchaseOrderId);
        return purchaseService.receivePurchaseOrder(purchaseOrderId)
                .map(mapper::toResponseDto);
    }

    @GetMapping("/{purchaseOrderId}")
    public Mono<PurchaseResponseDTO> getById(@PathVariable ("purchaseOrderId") UUID purchaseOrderId) {
        log.info("🔍 [PURCHASES] Fetching purchase order {}", purchaseOrderId);
        return purchaseService.receivePurchaseOrder(purchaseOrderId)
                .map(mapper::toResponseDto);
    }
}
