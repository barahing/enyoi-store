package com.store.carts_microservice.application.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.store.carts_microservice.domain.ports.out.ICartReportPersistencePort;
import com.store.carts_microservice.infrastructure.web.dto.EmailSendRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AbandonedCartReportService {

    private final ICartReportPersistencePort reportPort;
    private final WebClient webClient = WebClient.builder().build();


    @Value("${app.notifications.base-url}")
    private String notificationsBaseUrl;

    private static final String REPORTS_DIR = "D:/Enyoi - Acelera TI/enyoi-store/reports/abandoned_carts_reports/";

    public Mono<File> generateReport() {
        return reportPort.findActiveCartsWithoutOrder()
                .collectList()
                .flatMap(carts -> {
                    if (carts.isEmpty()) {
                        log.warn("⚠️ No hay carritos abandonados para reportar");
                        return Mono.error(new RuntimeException("No hay carritos abandonados"));
                    }

                    try {
                        File dir = new File(REPORTS_DIR);
                        if (!dir.exists()) dir.mkdirs();

                        String filename = "abandoned_carts_" + LocalDateTime.now().toString().replace(":", "-") + ".pdf";
                        File file = new File(dir, filename);

                        // PDF generation
                        Document doc = new Document(PageSize.A4);
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        PdfWriter.getInstance(doc, out);
                        doc.open();

                        Font title = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
                        Font header = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
                        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);

                        doc.add(new Paragraph("🛒 Reporte de Carritos Abandonados", title));
                        doc.add(new Paragraph("Fecha: " + LocalDateTime.now(), normal));
                        doc.add(Chunk.NEWLINE);

                        PdfPTable table = new PdfPTable(5);
                        table.setWidthPercentage(100);
                        table.addCell("Cart ID");
                        table.addCell("Cliente ID");
                        table.addCell("Total");
                        table.addCell("Creado");
                        table.addCell("Actualizado");

                        carts.forEach(c -> {
                            table.addCell(c.getId().toString());
                            table.addCell(String.valueOf(c.getClientId()));
                            table.addCell("$" + c.getTotal());
                            table.addCell(String.valueOf(c.getCreatedDate()));
                            table.addCell(String.valueOf(c.getUpdatedDate()));
                        });

                        doc.add(table);
                        doc.close();

                        try (FileOutputStream fos = new FileOutputStream(file)) {
                            fos.write(out.toByteArray());
                        }

                        log.info("✅ Reporte de carritos abandonados generado: {}", file.getAbsolutePath());
                        return Mono.just(file);

                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Error generando PDF: " + e.getMessage()));
                    }
                });
    }

    public Mono<Void> generateAndSendEmail() {
        return generateReport()
                .flatMap(file -> {
                    EmailSendRequest request = new EmailSendRequest(
                            "rikbarahona@gmail.com",
                            "Reporte de Carritos Abandonados",
                            """
                            <h2>Reporte de Carritos Abandonados</h2>
                            <p>Adjunto encontrarás el reporte con los carritos que no se completaron.</p>
                            """,
                            file.getAbsolutePath()
                    );

                    log.info("📤 Enviando solicitud de correo a notifications: {}", notificationsBaseUrl);

                    return webClient.post()
                            .uri(notificationsBaseUrl + "/api/notifications/abandoned-carts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(request)
                            .retrieve()
                            .bodyToMono(Void.class)
                            .doOnSuccess(v -> log.info("✅ Correo de carritos abandonados enviado correctamente"))
                            .doOnError(e -> log.error("❌ Error enviando correo: {}", e.getMessage()));
                });
    }
}
