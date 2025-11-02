package com.store.purchases_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import com.store.purchases_microservice.domain.model.PurchaseOrder;
import com.store.purchases_microservice.domain.model.PurchaseStatus;
import com.store.purchases_microservice.infrastructure.persistence.entity.PurchaseOrderEntity;

@Mapper(componentModel = "spring")
public interface IPurchaseOrderEntityMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "purchaseStatusToString")
    PurchaseOrderEntity toEntity(PurchaseOrder domain);

    @Mapping(target = "status", source = "status", qualifiedByName = "stringToPurchaseStatus")
    PurchaseOrder toDomain(PurchaseOrderEntity entity);

    @Named("purchaseStatusToString")
    default String purchaseStatusToString(PurchaseStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToPurchaseStatus")
    default PurchaseStatus stringToPurchaseStatus(String status) {
        return status != null ? PurchaseStatus.valueOf(status) : null;
    }
}