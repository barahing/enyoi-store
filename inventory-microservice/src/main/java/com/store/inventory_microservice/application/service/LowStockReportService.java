package com.store.inventory_microservice.application.service;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.store.common.events.LowStockAlertEvent;
import com.store.inventory_microservice.domain.model.ProductStock;
import com.store.inventory_microservice.domain.ports.out.IProductStockPersistencePort;
import com.store.inventory_microservice.domain.ports.out.IEventPublisherPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class LowStockReportService {

    private final IProductStockPersistencePort persistencePort;
    private final StockThresholdConfigService configService;
    private final IEventPublisherPort eventPublisherPort;

    private final String REPORT_DIR = "reports/low_stock_reports";

    public Mono<Void> generateReports() {
        return configService.getThresholdValue()
            .flatMapMany(threshold ->
                persistencePort.findAllStocks()
                    .filter(stock -> stock.getCurrentStock() < threshold)
                    .collectList()
                    .flatMapMany(lowStockProducts -> {
                        if (lowStockProducts.isEmpty()) {
                            log.info("ℹ️ No low-stock products found (threshold={}), skipping report.", threshold);
                            return Mono.empty();
                        }

                        log.info("⚠️ Found {} products below threshold {}", lowStockProducts.size(), threshold);
                        return generateCsvAndPdf(lowStockProducts, threshold).thenMany(Mono.empty());
                    })
            )
            .then();
    }

    private Mono<Void> generateCsvAndPdf(List<ProductStock> lowStockProducts, int threshold) {
        try {
            File dir = new File(REPORT_DIR);
            if (!dir.exists()) dir.mkdirs();

            String timestamp = LocalDateTime.now().toString().replace(":", "-");
            String csvFileName = REPORT_DIR + "/low_stock_report_" + timestamp + ".csv";
            String pdfFileName = REPORT_DIR + "/low_stock_report_" + timestamp + ".pdf";

            // ✅ CSV
            try (FileWriter writer = new FileWriter(csvFileName)) {
                writer.write("productId,currentStock,reservedStock,availableStock\n");
                for (ProductStock stock : lowStockProducts) {
                    writer.write(stock.getProductId() + "," +
                                 stock.getCurrentStock() + "," +
                                 stock.getReservedStock() + "," +
                                 stock.getAvailableStock() + "\n");
                }
            }

            // ✅ PDF
            generatePdfReport(lowStockProducts, pdfFileName);

            log.info("📄 [REPORT] Generated CSV: {}", csvFileName);
            log.info("📄 [REPORT] Generated PDF: {}", pdfFileName);

            LowStockAlertEvent event = new LowStockAlertEvent(
                UUID.randomUUID(),                     
                lowStockProducts.size(),               
                threshold                              
            );

            return eventPublisherPort.publishLowStockAlertEvent(event)
                .doOnSuccess(v -> log.info("📤 [EVENT] LowStockAlertEvent sent ({} products under threshold {})",
                                           lowStockProducts.size(), threshold))
                .doOnError(e -> log.error("❌ [EVENT] Failed to send LowStockAlertEvent: {}", e.getMessage()));

        } catch (Exception e) {
            log.error("❌ Failed to generate low-stock report: {}", e.getMessage());
            return Mono.error(e);
        }
    }

    private void generatePdfReport(List<ProductStock> products, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        document.add(new Paragraph("📦 Low Stock Report", titleFont));
        document.add(new Paragraph("Generated at: " + LocalDateTime.now(), cellFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.addCell(new PdfPCell(new Phrase("Product ID", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Current Stock", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Reserved Stock", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Available Stock", headerFont)));

        for (ProductStock stock : products) {
            table.addCell(new Phrase(stock.getProductId().toString(), cellFont));
            table.addCell(new Phrase(String.valueOf(stock.getCurrentStock()), cellFont));
            table.addCell(new Phrase(String.valueOf(stock.getReservedStock()), cellFont));
            table.addCell(new Phrase(String.valueOf(stock.getAvailableStock()), cellFont));
        }

        document.add(table);
        document.close();
    }
}
