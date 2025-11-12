package com.store.orders_microservice.infrastructure.scheduler;

import com.store.orders_microservice.application.service.WeeklySalesReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklySalesReportScheduler {

    private final WeeklySalesReportService reportService;

    @Scheduled(cron = "0 0 8 * * MON", zone = "America/Mexico_City")
    public void generateWeeklyReport() {
        log.info("🗓️ [CRON] Ejecutando generación semanal de reporte de ventas...");
        reportService.generateWeeklyReport()
            .doOnNext(report -> log.info("✅ Reporte generado: {} órdenes, ${}", 
                    report.getTotalOrders(), report.getTotalSalesAmount()))
            .subscribe();
    }
}
