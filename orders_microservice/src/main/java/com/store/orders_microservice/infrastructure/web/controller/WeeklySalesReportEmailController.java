package com.store.orders_microservice.infrastructure.web.controller;

import com.store.orders_microservice.application.service.WeeklySalesReportService;
import com.store.orders_microservice.domain.model.WeeklySalesReport;
import com.store.orders_microservice.infrastructure.web.dto.EmailSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/reports/sales/weekly")
@RequiredArgsConstructor
@Slf4j
public class WeeklySalesReportEmailController {

    private final WeeklySalesReportService reportService;
    private final WebClient.Builder webClientBuilder;

    @Value("${app.notifications.base-url}")
    private String notificationsUrl;

    @Value("${app.notifications.test-recipient}")
    private String recipientEmail;

    @Value("${app.reports.sales-dir}")
    private String reportsDir;

    @PostMapping("/send-email")
    public Mono<String> generateAndEmailWeeklyReport() {
        return reportService.generateWeeklyReport()
                .flatMap(report -> savePdf(report)
                        .flatMap(this::sendEmail))
                .doOnSuccess(r -> log.info("✅ Reporte semanal enviado correctamente"))
                .doOnError(e -> log.error("❌ Error enviando reporte semanal: {}", e.getMessage()));
    }

    private Mono<File> savePdf(WeeklySalesReport report) {
        try {
            File dir = new File(reportsDir);
            if (!dir.exists()) dir.mkdirs();

            String filename = "sales_report_" + LocalDateTime.now().toString().replace(":", "-") + ".pdf";
            File file = new File(dir, filename);

            byte[] pdfBytes = generatePdf(report);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(pdfBytes);
            }

            log.info("📄 Reporte PDF generado: {}", file.getAbsolutePath());
            return Mono.just(file);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error generando PDF: " + e.getMessage()));
        }
    }

    private byte[] generatePdf(WeeklySalesReport report) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph("📊 Reporte Semanal de Ventas", titleFont));
        document.add(new Paragraph("Periodo: " + report.getWeekStart() + " a " + report.getWeekEnd(), normalFont));
        document.add(new Paragraph("Total de órdenes: " + report.getTotalOrders(), normalFont));
        document.add(new Paragraph("Total vendido: $" + report.getTotalSalesAmount(), normalFont));
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Top Productos", headerFont));
        PdfPTable productTable = new PdfPTable(2);
        productTable.setWidthPercentage(100);
        productTable.addCell("Producto ID");
        productTable.addCell("Cantidad vendida");
        for (var p : report.getTopProducts()) {
            productTable.addCell(p.getProductId());
            productTable.addCell(String.valueOf(p.getQuantitySold()));
        }
        document.add(productTable);
        document.add(Chunk.NEWLINE);

        document.add(new Paragraph("Top Clientes", headerFont));
        PdfPTable clientTable = new PdfPTable(2);
        clientTable.setWidthPercentage(100);
        clientTable.addCell("Cliente ID");
        clientTable.addCell("Número de órdenes");
        for (var c : report.getTopCustomers()) {
            clientTable.addCell(c.getClientId());
            clientTable.addCell(String.valueOf(c.getOrdersCount()));
        }
        document.add(clientTable);

        document.close();
        return out.toByteArray();
    }

    private Mono<String> sendEmail(File latestFile) {
        if (latestFile == null || !latestFile.exists()) {
            return Mono.error(new RuntimeException("No se encontró el archivo PDF"));
        }

        String subject = "📊 Reporte Semanal de Ventas - " + LocalDateTime.now().toLocalDate();
        String html = """
                <h2>📈 Reporte Semanal de Ventas</h2>
                <p>Adjunto encontrarás el reporte con las estadísticas de ventas de esta semana.</p>
                <p>Archivo: <b>%s</b></p>
                <br/>
                <p>— Arka Store</p>
                """.formatted(latestFile.getName());

        EmailSendRequest request = new EmailSendRequest(
                recipientEmail,
                subject,
                html,
                latestFile.getAbsolutePath()
        );

        return webClientBuilder.build()
                .post()
                .uri(notificationsUrl + "/api/notifications/sales-report")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn("Correo enviado a " + recipientEmail + " con " + latestFile.getName());
    }
}
