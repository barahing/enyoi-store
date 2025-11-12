package com.store.carts_microservice.infrastructure.web.controller;

import com.store.carts_microservice.application.service.AbandonedCartReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/reports/carts/abandoned")
@RequiredArgsConstructor
@Slf4j
public class AbandonedCartReportController {

    private final AbandonedCartReportService reportService;

    @PostMapping("/generate")
    public Mono<String> generateReport() {
        return reportService.generateReport()
                .map(file -> "Reporte generado en: " + file.getAbsolutePath());
    }

    @PostMapping("/send-email")
    public Mono<String> generateAndSendEmail() {
        return reportService.generateAndSendEmail()
                .thenReturn("✅ Reporte generado y correo enviado correctamente")
                .doOnError(e -> log.error("❌ Error enviando correo: {}", e.getMessage()));
    }
}
