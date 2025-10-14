package com.store.orders_microservice.infrastructure.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.store.orders_microservice.domain.exception.OrderDomainException;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrderDomainException.class)
    public ResponseEntity<Map<String, Object>> handleOrderDomainException(OrderDomainException ex) {
        Map<String, Object> body = Map.of(
            "timestamp", LocalDateTime.now(),
            "error", ex.getClass().getSimpleName(),
            "message", ex.getMessage(),
            "orderId", ex.getOrderId(),
            "status", ex.getCurrentStatus(),
            "code", "ORDER_VALIDATION_ERROR"
        );

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> body = Map.of(
            "timestamp", LocalDateTime.now(),
            "error", ex.getClass().getSimpleName(),
            "message", ex.getMessage(),
            "code", "INTERNAL_ERROR"
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
