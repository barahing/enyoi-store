package com.store.payments_microservice.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.store.payments_microservice.domain.model.Payment;
import com.store.payments_microservice.domain.model.PaymentStatus;
import com.store.payments_microservice.infrastructure.persistence.entity.PaymentEntity;

@Mapper(componentModel = "spring")
public interface IPaymentEntityMapper {

    @Mapping(target = "status", source = "status", qualifiedByName = "paymentStatusToString")
    PaymentEntity toEntity(Payment domain);


    @Mapping(target = "status", source = "status", qualifiedByName = "stringToPaymentStatus")
    Payment toDomain(PaymentEntity entity);


    @Named("paymentStatusToString")
    default String paymentStatusToString(PaymentStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToPaymentStatus")
    default PaymentStatus stringToPaymentStatus(String status) {
        return status != null ? PaymentStatus.valueOf(status) : null;
    }
}
