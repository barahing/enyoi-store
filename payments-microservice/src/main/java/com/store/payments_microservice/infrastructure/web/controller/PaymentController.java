package com.store.payments_microservice.infrastructure.web.controller;

import com.store.common.commands.ProcessPaymentCommand;
import com.store.payments_microservice.domain.ports.in.IPaymentServicePort;
import com.store.payments_microservice.infrastructure.web.dto.PaymentResponseDto;
import com.store.payments_microservice.infrastructure.web.dto.ProcessPaymentRequestDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final IPaymentServicePort paymentService;

    @PostMapping("/process")
    public Mono<PaymentResponseDto> processPayment(@RequestBody ProcessPaymentRequestDto request) {
        log.info("💰 [PAYMENTS] Processing payment for order: {} with method: {}", 
                request.getOrderId(), request.getPaymentMethod());
        
        ProcessPaymentCommand command = new ProcessPaymentCommand(
            request.getOrderId(),
            request.getUserId(),
            request.getAmount(),
            request.getPaymentMethod()
        );

        return paymentService.processOrderPayment(command)
            .thenReturn(
                PaymentResponseDto.builder()
                    .status("SUCCESS")
                    .message("Payment processed successfully")
                    .orderId(request.getOrderId())
                    .timestamp(LocalDateTime.now())
                    .build()
            )
            .onErrorResume(ex -> {
                log.error("❌ [PAYMENTS] Payment processing failed for order {}: {}", 
                        request.getOrderId(), ex.getMessage());
                return Mono.just(
                    PaymentResponseDto.builder()
                        .status("FAILED")
                        .message(ex.getMessage() != null ? ex.getMessage() : "Unexpected error")
                        .orderId(request.getOrderId())
                        .timestamp(LocalDateTime.now())
                        .build()
                );
            });
    }

    @GetMapping
    public Mono<String> healthCheck() {
        return Mono.just("Payments Microservice is up and running!");
    }
}
