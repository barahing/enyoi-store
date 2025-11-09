package com.store.inventory_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.store.inventory_microservice.domain.model.StockReservation;
import com.store.inventory_microservice.infrastructure.persistence.entity.StockReservationEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IStockReservationMapper {
    StockReservationEntity toEntity(StockReservation domain);
    StockReservation toDomain(StockReservationEntity entity);
}
