package com.store.payments_microservice.application.exception;

// Usamos RuntimeException ya que estamos en un contexto reactivo/de eventos.
public class PaymentRefusedException extends RuntimeException {
    public PaymentRefusedException(String message) {
        super(message);
    }
}