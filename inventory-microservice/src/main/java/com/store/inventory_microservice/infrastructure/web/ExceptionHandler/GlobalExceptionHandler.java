package com.store.inventory_microservice.infrastructure.web.ExceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.store.inventory_microservice.domain.exception.ProductAlreadyExistsException;
import com.store.inventory_microservice.domain.exception.ProductCatalogMismatchException;
import com.store.inventory_microservice.domain.exception.StockNotFoundException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<String> handleProductAlreadyExistsException(ProductAlreadyExistsException ex) {
        log.warn("Conflict error: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.CONFLICT) 
            .body(ex.getMessage());
    }
    
    @ExceptionHandler(ProductCatalogMismatchException.class)
    public ResponseEntity<String> handleProductCatalogMismatchException(ProductCatalogMismatchException ex) {
        log.warn("Bad Request error (Catalog Mismatch): {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST) 
            .body(ex.getMessage());
    }

    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<String> handleStockNotFoundException(StockNotFoundException ex) {
        log.warn("Not Found error: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND) 
            .body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        log.error("Internal Server Error: {}", ex.getMessage(), ex);
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR) 
            .body("An unexpected error occurred: " + ex.getMessage());
    }
}