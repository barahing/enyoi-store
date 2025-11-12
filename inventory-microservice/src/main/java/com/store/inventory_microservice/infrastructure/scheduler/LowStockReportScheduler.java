package com.store.inventory_microservice.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.store.inventory_microservice.application.service.LowStockReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockReportScheduler {

    private final LowStockReportService reportService;

    @Scheduled(fixedRate = 600_000)
    public void generateReportJob() {
        log.info("🕒 [SCHEDULER] Checking for low-stock products...");
        reportService.generateReports()
            .subscribe(
                v -> log.info("✅ [SCHEDULER] Report generation completed."),
                e -> log.error("❌ [SCHEDULER] Error generating report: {}", e.getMessage())
            );
    }
}
