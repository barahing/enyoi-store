package com.store.orders_microservice.infrastructure.persistence.adapter;

import com.store.orders_microservice.domain.ports.out.IOrderReportPersistencePort;
import com.store.orders_microservice.infrastructure.persistence.entity.OrderEntity;
import com.store.orders_microservice.infrastructure.persistence.entity.OrderItemEntity;
import com.store.orders_microservice.infrastructure.persistence.repository.IOrderItemR2dbcRepository;
import com.store.orders_microservice.infrastructure.persistence.repository.IOrderR2dbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OrderReportR2dbcAdapter implements IOrderReportPersistencePort {

    private final IOrderR2dbcRepository orderRepo;
    private final IOrderItemR2dbcRepository itemRepo;

    private static final List<String> VALID_SALE_STATUSES = List.of(
            "PAYMENT_APPROVED", "CONFIRMED", "SHIPPED", "DELIVERED"
    );

    @Override
    public Flux<OrderEntity> findCompletedOrdersBetween(LocalDateTime start, LocalDateTime end) {
        return orderRepo.findAll()
                .filter(o ->
                        o.getCreatedDate() != null &&
                        VALID_SALE_STATUSES.contains(o.getStatus()) &&
                        !o.getCreatedDate().isBefore(start) &&
                        !o.getCreatedDate().isAfter(end)
                );
    }

    @Override
    public Flux<OrderItemEntity> findOrderItemsByOrderIds(Set<UUID> orderIds) {
        return itemRepo.findAll()
                .filter(i -> orderIds.contains(i.getOrderId()));
    }
}
