package com.store.payments_microservice.application.exception;

public class PaymentRefusedException extends RuntimeException {
    public PaymentRefusedException(String message) {
        super(message);
    }
}