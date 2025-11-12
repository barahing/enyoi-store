package com.store.notifications_microservice.infrastructure.messaging.consumer;

import com.store.common.events.LowStockAlertEvent;
import com.store.notifications_microservice.domain.ports.out.IBrevoSenderPorts;
import com.store.notifications_microservice.infrastructure.email.adapter.BrevoSenderAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockAlertConsumer {

    private final IBrevoSenderPorts brevoSender;

    @Value("${app.inventory.lowstock-report-url:http://localhost:8085/api/reports/low-stock/latest}")
    private String latestReportUrl;

    @Value("${app.notifications.test-recipient:rikbarahona@gmail.com}")
    private String testRecipient;

    @Value("${app.reports.path:D:/Enyoi - Acelera TI/enyoi-store/reports/low_stock_reports}")
    private String reportsPath;

    @RabbitListener(queues = "inventory.lowstock.alert.queue")
    public void handleLowStockAlert(LowStockAlertEvent event) {
        log.info("📩 [RECEIVED] LowStockAlertEvent | totalLowStock={} | threshold={}",
                event.getCurrentStock(), event.getReorderLevel());

        String subject = "⚠️ Alerta de Bajo Stock - " + event.getCurrentStock() + " productos afectados";
        String body = """
            <h2>⚠️ Alerta de Bajo Stock</h2>
            <p>Se han detectado <b>%d productos</b> con un stock por debajo del umbral configurado (<b>%d</b>).</p>
            <p>Puedes descargar el reporte completo aquí:</p>
            <p><a href="%s" target="_blank">Descargar reporte de bajo stock (PDF)</a></p>
            <br/>
            <p>Este mensaje fue generado automáticamente por el sistema Arka Store.</p>
            """.formatted(
                event.getCurrentStock(),
                event.getReorderLevel(),
                latestReportUrl
            );

        File latestReport = getLatestReportFile();
        boolean hasAttachment = latestReport != null && latestReport.exists();

        if (hasAttachment && brevoSender instanceof BrevoSenderAdapter adapter) {
            log.info("📎 Adjunto encontrado: {}", latestReport.getName());
            adapter.sendTransactionalEmail(testRecipient, subject, body, latestReport)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(s -> log.info("➡️ [ACTION] Sending low-stock email with attachment to {}", testRecipient))
                .doOnSuccess(v -> log.info("✅ [SENT] Low-stock alert with attachment sent to {}", testRecipient))
                .doOnError(e -> log.error("❌ [ERROR] Failed to send low-stock email with attachment: {}", e.getMessage()))
                .subscribe();
        } else {
            log.warn("⚠️ No se encontró archivo PDF, enviando correo sin adjunto...");
            brevoSender.sendTransactionalEmail(testRecipient, subject, body)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(s -> log.info("➡️ [ACTION] Sending low-stock email (no attachment) to {}", testRecipient))
                .doOnSuccess(v -> log.info("✅ [SENT] Low-stock alert email sent to {}", testRecipient))
                .doOnError(e -> log.error("❌ [ERROR] Failed to send low-stock email: {}", e.getMessage()))
                .subscribe();
        }
    }

    private File getLatestReportFile() {
        File dir = new File(reportsPath);
        if (!dir.exists() || !dir.isDirectory()) {
            log.warn("⚠️ Reports directory not found: {}", reportsPath);
            return null;
        }

        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
        if (files == null || files.length == 0) {
            log.info("ℹ️ No se encontraron reportes PDF en {}", reportsPath);
            return null;
        }

        return Arrays.stream(files)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }
}
