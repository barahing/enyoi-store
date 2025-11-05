package com.store.carts_microservice.domain.model;

public enum CartStatus {
    ACTIVE,
    CONVERTING,
    CONVERTED_TO_ORDER,
    FAILED,
    ABANDONED
}
