package com.store.payments_microservice.infrastructure.web.controller;

import com.store.common.commands.ProcessPaymentCommand;
import com.store.payments_microservice.domain.ports.in.IPaymentServicePort;
import com.store.payments_microservice.infrastructure.web.dto.ProcessPaymentRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final IPaymentServicePort paymentService;

    @PostMapping("/process")
    public Mono<String> processPayment(@RequestBody ProcessPaymentRequestDto request) {
        log.info("💰 [PAYMENTS] Processing payment for order: {} with method: {}", 
                request.getOrderId(), request.getPaymentMethod());
        
        ProcessPaymentCommand command = new ProcessPaymentCommand(
            request.getOrderId(),
            request.getUserId(),
            request.getAmount(),
            request.getPaymentMethod()
        );
        
        return paymentService.processOrderPayment(command)
            .thenReturn("Payment processing initiated for order: " + request.getOrderId())
            .doOnSuccess(result -> log.info("✅ [PAYMENTS] Payment processing completed for order: {}", request.getOrderId()))
            .doOnError(error -> log.error("❌ [PAYMENTS] Payment processing failed for order {}: {}", 
                request.getOrderId(), error.getMessage()));
    }

    @GetMapping()
    public Mono<String> healthCheck() {
        return Mono.just("Payments Microservice is up and running!");
    }
}