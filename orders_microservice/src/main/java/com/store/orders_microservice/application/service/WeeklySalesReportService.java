package com.store.orders_microservice.application.service;

import com.store.orders_microservice.domain.model.WeeklySalesReport;
import com.store.orders_microservice.domain.ports.out.IOrderReportPersistencePort;
import com.store.orders_microservice.infrastructure.persistence.entity.OrderEntity;
import com.store.orders_microservice.infrastructure.persistence.entity.OrderItemEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklySalesReportService {

    private final IOrderReportPersistencePort reportPersistence;

    public Mono<WeeklySalesReport> generateWeeklyReport() {
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        LocalDateTime startDateTime = startOfWeek.atStartOfDay();
        LocalDateTime endDateTime = endOfWeek.atTime(23, 59, 59);

        log.info("📅 Generando reporte semanal: {} -> {}", startOfWeek, endOfWeek);

        return reportPersistence.findCompletedOrdersBetween(startDateTime, endDateTime)
            .collectList()
            .flatMap(orders -> {
                if (orders.isEmpty()) {
                    return Mono.just(emptyReport(startOfWeek, endOfWeek));
                }

                BigDecimal totalSales = orders.stream()
                        .map(OrderEntity::getTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Set<UUID> orderIds = orders.stream().map(OrderEntity::getId).collect(Collectors.toSet());

                return reportPersistence.findOrderItemsByOrderIds(orderIds)
                        .collectList()
                        .map(items -> buildReport(startOfWeek, endOfWeek, orders, items, totalSales));
            });
    }

    private WeeklySalesReport buildReport(LocalDate start, LocalDate end,
                                          List<OrderEntity> orders,
                                          List<OrderItemEntity> items,
                                          BigDecimal totalSales) {

        Map<UUID, Long> productTotals = items.stream()
                .collect(Collectors.groupingBy(OrderItemEntity::getProductId, Collectors.summingLong(OrderItemEntity::getQuantity)));

        Map<UUID, Long> customerOrders = orders.stream()
                .collect(Collectors.groupingBy(OrderEntity::getClientId, Collectors.counting()));

        List<WeeklySalesReport.TopProduct> topProducts = productTotals.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new WeeklySalesReport.TopProduct(e.getKey().toString(), e.getValue()))
                .collect(Collectors.toList());

        List<WeeklySalesReport.TopCustomer> topCustomers = customerOrders.entrySet().stream()
                .sorted(Map.Entry.<UUID, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> new WeeklySalesReport.TopCustomer(e.getKey().toString(), e.getValue()))
                .collect(Collectors.toList());

        return WeeklySalesReport.builder()
                .weekStart(start)
                .weekEnd(end)
                .totalSalesAmount(totalSales)
                .totalOrders((long) orders.size())
                .topProducts(topProducts)
                .topCustomers(topCustomers)
                .build();
    }

    private WeeklySalesReport emptyReport(LocalDate start, LocalDate end) {
        return WeeklySalesReport.builder()
                .weekStart(start)
                .weekEnd(end)
                .totalSalesAmount(BigDecimal.ZERO)
                .totalOrders(0L)
                .topProducts(Collections.emptyList())
                .topCustomers(Collections.emptyList())
                .build();
    }
}
