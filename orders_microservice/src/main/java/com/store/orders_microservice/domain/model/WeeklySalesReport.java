package com.store.orders_microservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklySalesReport {
    private LocalDate weekStart;
    private LocalDate weekEnd;
    private BigDecimal totalSalesAmount;
    private Long totalOrders;
    private List<TopProduct> topProducts;
    private List<TopCustomer> topCustomers;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopProduct {
        private String productId;
        private Long quantitySold;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TopCustomer {
        private String clientId;
        private Long ordersCount;
    }
}
