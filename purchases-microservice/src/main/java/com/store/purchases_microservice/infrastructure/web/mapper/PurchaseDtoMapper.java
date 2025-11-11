package com.store.purchases_microservice.infrastructure.web.mapper;

import com.store.purchases_microservice.domain.model.PurchaseOrder;
import com.store.purchases_microservice.infrastructure.web.dto.PurchaseRequestDTO;
import com.store.purchases_microservice.infrastructure.web.dto.PurchaseResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class PurchaseDtoMapper {

    public PurchaseOrder toDomain(PurchaseRequestDTO dto) {
        return PurchaseOrder.createNew(
            dto.getSupplierName(),
            dto.getProductId(),
            dto.getQuantity(),
            dto.getUnitCost()
        );
    }

    public PurchaseResponseDTO toResponseDto(PurchaseOrder order) {
        return PurchaseResponseDTO.builder()
            .id(order.getId())
            .supplierName(order.getSupplierName())
            .productId(order.getProductId())
            .quantity(order.getQuantity())
            .unitCost(order.getUnitCost())
            .status(order.getStatus())
            .orderDate(order.getOrderDate())
            .deliveryDate(order.getDeliveryDate())
            .build();
    }
}
