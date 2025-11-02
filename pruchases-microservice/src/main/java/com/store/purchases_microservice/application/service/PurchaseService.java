package com.store.purchases_microservice.application.service;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import com.store.purchases_microservice.domain.model.PurchaseOrder;
import com.store.purchases_microservice.domain.ports.in.IPurchaseServicePorts;
import com.store.purchases_microservice.domain.ports.out.IEventPublisherPorts;
import com.store.purchases_microservice.domain.ports.out.IPurchaseOrderPersistencePort;
import com.store.common.events.StockReceivedEvent;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PurchaseService implements IPurchaseServicePorts {

    private final IPurchaseOrderPersistencePort persistencePorts;
    private final IEventPublisherPorts eventPublisherPorts;

    @Override
    public Mono<PurchaseOrder> createPurchaseOrder(String supplierName, UUID productId, Integer quantity, BigDecimal unitCost) {
        PurchaseOrder newOrder = PurchaseOrder.createNew(supplierName, productId, quantity, unitCost);
        return persistencePorts.save(newOrder);
    }

    @Override
    public Mono<PurchaseOrder> receivePurchaseOrder(UUID purchaseOrderId) {
        return persistencePorts.findById(purchaseOrderId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Purchase Order not found.")))
            .flatMap(pendingOrder -> {
                
                PurchaseOrder receivedOrder = pendingOrder.markAsReceived();
                
                return persistencePorts.save(receivedOrder)
                    .flatMap(savedOrder -> {
                        
                        StockReceivedEvent event = new StockReceivedEvent(
                            savedOrder.getProductId(), 
                            savedOrder.getQuantity(), 
                            savedOrder.getId()
                        );
                        
                        return eventPublisherPorts.publishStockReceivedEvent(event)
                            .thenReturn(savedOrder); 
                    });
            });
    }
}