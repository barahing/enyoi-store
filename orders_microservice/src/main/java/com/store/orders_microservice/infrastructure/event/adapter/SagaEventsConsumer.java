package com.store.orders_microservice.infrastructure.event.adapter;

import java.util.UUID;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.store.common.events.CartConvertedEvent;
import com.store.common.events.OrderConfirmedEvent;
import com.store.common.events.OrderCreatedEvent;
import com.store.common.events.PaymentFailedEvent;
import com.store.common.events.PaymentProcessedEvent;
import com.store.common.events.StockReservationFailedEvent;
import com.store.common.events.StockReservedEvent;
import com.store.common.commands.ReserveStockCommand;
import com.store.common.dto.ProductStockDTO;
import com.store.orders_microservice.domain.exception.OrderNotFoundException;
import com.store.orders_microservice.domain.factory.OrderFactory;
import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.model.OrderItem; 
import com.store.orders_microservice.domain.model.OrderStatus;
import com.store.orders_microservice.domain.ports.out.IEventPublisherPort;
import com.store.orders_microservice.domain.ports.out.IOrderRepositoryPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class SagaEventsConsumer {

    private final IOrderRepositoryPort orderRepositoryPort;
    private final IEventPublisherPort eventPublisherPort;
    
    // --- MANEJO DE EVENTOS DE PAGO ---
    
    @RabbitListener(queues = "${app.rabbitmq.payment-processed-queue}")
    public void handlePaymentProcessed(PaymentProcessedEvent event) {
        this.processSagaEvent(event.getOrderId(), "PaymentProcessedEvent", 
            order -> {

                // 🚦 Validar estados en los que se permite pago
                if (order.getStatus() != OrderStatus.STOCK_RESERVED && order.getStatus() != OrderStatus.CONFIRMED) {
                    log.warn("🚫 [ORDERS] Ignoring PaymentProcessedEvent for order {} (invalid status: {})",
                            order.getOrderId(), order.getStatus());

                    // Opcional: publicar un evento de rechazo de pago
                    return eventPublisherPort.publishOrderCancelledEvent(
                        new com.store.common.events.OrderCancelledEvent(
                            order.getOrderId(),
                            order.getClientId(),
                            "Payment received in invalid state: " + order.getStatus()
                        )
                    );
                }

                // ✅ Transición válida: aplicar el cambio
                order.handlePaymentApproved();
                log.info("✅ [ORDERS] Order {} status updated to PAYMENT_APPROVED", order.getOrderId());

                // Si ahora está CONFIRMED (ambos eventos llegaron)
                if (order.getStatus() == OrderStatus.CONFIRMED) {
                    return eventPublisherPort.publishOrderConfirmedEvent(
                        new com.store.common.events.OrderConfirmedEvent(order.getOrderId(), order.getClientId())
                    );
                }

                return Mono.empty();
            }
        ).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }



    @RabbitListener(queues = "${app.rabbitmq.payment-failed-queue}")
    public void handlePaymentFailure(PaymentFailedEvent event) {
        this.processSagaEvent(event.getOrderId(), "PaymentFailedEvent", 
            order -> {
                order.handlePaymentFailed(event.getReason()); 
                
                List<ProductStockDTO> productsToRelease = order.getItems().stream()
                    .map(item -> new ProductStockDTO(item.productId(), item.quantity()))
                    .collect(Collectors.toList());
                
                // 1. Publicar notificación de cancelación
                Mono<Void> cancelNotification = eventPublisherPort.publishOrderCancelledEvent(
                    new com.store.common.events.OrderCancelledEvent(order.getOrderId(), order.getClientId(), event.getReason())
                );
                
                // 🔥 COMENTAR TEMPORALMENTE: No liberar stock para permitir reintentos
                /*
                ReleaseStockCommand releaseCommand = new ReleaseStockCommand(
                    order.getOrderId(), 
                    "Saga rollback: Payment failed.",
                    productsToRelease
                );
                
                Mono<Void> releaseStock = eventPublisherPort.publishReleaseStockCommand(releaseCommand);
                
                return cancelNotification.then(releaseStock);
                */
                
                // 🔥 SOLO notificar cancelación, mantener stock reservado
                log.warn("⚠️ Payment failed for order {}, but keeping stock reserved for potential retry", order.getOrderId());
                return cancelNotification;
            }
        ).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }
    
    // --- MANEJO DE EVENTOS DE INVENTARIO ---

    @RabbitListener(queues = "${app.rabbitmq.stock-reserved-queue}")
    public void handleStockReserved(StockReservedEvent event) {
        this.processSagaEvent(event.orderId(), "StockReservedEvent", 
            order -> {
                order.handleStockReserved();
                
                // ✅ SOLO actualizar estado, NO publicar ProcessPaymentCommand
                // Si la orden está CONFIRMED (ambos han llegado), publicamos el evento final.
                if (order.getStatus() == OrderStatus.CONFIRMED) {
                    return eventPublisherPort.publishOrderConfirmedEvent(
                        new OrderConfirmedEvent(order.getOrderId(), order.getClientId())
                    );
                }
                return Mono.empty();
            }
        ).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }
    
    @RabbitListener(queues = "${app.rabbitmq.stock-reservation-failed-queue}")
    public void handleStockReservationFailed(StockReservationFailedEvent event) {
        this.processSagaEvent(event.orderId(), "StockReservationFailedEvent", 
            order -> {
                order.handleStockFailed(event.reason()); 
                
                // Solo se necesita notificación de cancelación.
                return eventPublisherPort.publishOrderCancelledEvent(
                    new com.store.common.events.OrderCancelledEvent(order.getOrderId(), order.getClientId(), event.reason())
                );
            }
        ).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    private Mono<Void> processSagaEvent(UUID orderId, String eventName, java.util.function.Function<Order, Mono<Void>> action) {
        return orderRepositoryPort.findById(orderId)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
            .flatMap(order -> {
                log.info("Processing {} for Order ID: {} (Status: {})", eventName, orderId, order.getStatus());
                
                Mono<Void> publishAction = action.apply(order);
                
                Mono<Order> saveAction = orderRepositoryPort.save(order);
                
                // Guardamos el estado de la orden y luego publicamos el siguiente evento/comando
                return saveAction.then(publishAction); 
            })
            .onErrorResume(e -> {
                log.error("Error processing Saga event {} for Order ID {}: {}", eventName, orderId, e.getMessage());
                return Mono.empty();
            });
    }

   @RabbitListener(queues = "${app.rabbitmq.cart-converted-queue}")
    public void handleCartConverted(CartConvertedEvent event) {
        log.info("🎯 [ORDERS] Received CartConvertedEvent for ClientId: {}", event.clientId());
        
        // 1️⃣ Crear la nueva orden
        orderRepositoryPort.save(
            OrderFactory.createNew(
                event.clientId(),
                event.items().stream()
                    .map(itemData -> OrderItem.create(
                        itemData.productId(),
                        itemData.quantity(),
                        itemData.price()
                    ))
                    .collect(Collectors.toList())
            )
        )
        .flatMap((Order savedOrder) -> {
            log.info("✅ [ORDERS] Order created with ID: {}", savedOrder.getOrderId());

            List<ProductStockDTO> productList = savedOrder.getItems().stream()
                .map(item -> new ProductStockDTO(item.productId(), item.quantity()))
                .collect(Collectors.toList());

            // 2️⃣ Publicar OrderCreatedEvent y ESPERAR a que se complete
            OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
            orderCreatedEvent.setOrderId(savedOrder.getOrderId());
            orderCreatedEvent.setUserId(savedOrder.getClientId());
            orderCreatedEvent.setAmount(savedOrder.getTotal());
            orderCreatedEvent.setProducts(productList);

            log.info("📨 [ORDERS] Publishing OrderCreatedEvent for orderId={}", savedOrder.getOrderId());
            
            return eventPublisherPort.publishOrderCreatedEvent(orderCreatedEvent)
                // 3️⃣ ESPERAR antes de reservar stock para dar tiempo a Carts
                .then(Mono.delay(Duration.ofMillis(1000))) // ⚡ 1 segundo de delay
                .then(Mono.defer(() -> {
                    // 4️⃣ Solo después del delay, publicar ReserveStockCommand
                    ReserveStockCommand reserveCommand = new ReserveStockCommand(savedOrder.getOrderId(), productList);
                    log.info("📦 [ORDERS] Publishing ReserveStockCommand for orderId={}", savedOrder.getOrderId());
                    return eventPublisherPort.publishReserveStockCommand(reserveCommand);
                }))
                .thenReturn(savedOrder);
        })
        .doOnError(error -> log.error("❌ [ORDERS] Failed to create order from CartConvertedEvent: {}", error.getMessage(), error))
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
    }
}