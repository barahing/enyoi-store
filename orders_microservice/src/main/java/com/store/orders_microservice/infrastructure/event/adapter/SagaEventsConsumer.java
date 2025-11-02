package com.store.orders_microservice.infrastructure.event.adapter;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.store.common.events.PaymentFailedEvent;
import com.store.common.events.PaymentProcessedEvent;
import com.store.common.events.StockReservationFailedEvent;
import com.store.common.events.StockReservedEvent;
import com.store.common.commands.ReleaseStockCommand;
import com.store.common.dto.ProductStockDTO;
import com.store.orders_microservice.domain.exception.OrderNotFoundException;
import com.store.orders_microservice.domain.model.Order;
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
                // ✅ AJUSTE: Usamos handlePaymentApproved para coherencia con el dominio
                order.handlePaymentApproved(); 
                
                // Si la orden está CONFIRMED (tanto pago como stock fueron exitosos), publicamos el evento final.
                // Esto maneja el caso de que el pago llegue antes o después de la reserva de stock.
                if (order.getStatus().toString().equals("CONFIRMED")) {
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
                
                // Mapear OrderItems a ProductStockDTOs para el comando de rollback
                List<ProductStockDTO> productsToRelease = order.getItems().stream()
                    .map(item -> new ProductStockDTO(item.productId(), item.quantity()))
                    .collect(Collectors.toList());
                
                // 1. Publicar notificación de cancelación
                Mono<Void> cancelNotification = eventPublisherPort.publishOrderCancelledEvent(
                    new com.store.common.events.OrderCancelledEvent(order.getOrderId(), order.getClientId(), event.getReason())
                );
                
                // 2. Publicar comando de liberación de stock (ROLLBACK)
                ReleaseStockCommand releaseCommand = new ReleaseStockCommand(
                    order.getOrderId(), 
                    "Saga rollback: Payment failed.",
                    productsToRelease
                );
                
                Mono<Void> releaseStock = eventPublisherPort.publishReleaseStockCommand(releaseCommand);
                
                // Retornar la secuencia de publicación: Cancelar y luego Rollback.
                return cancelNotification.then(releaseStock);
            }
        ).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }
    
    // --- MANEJO DE EVENTOS DE INVENTARIO ---

    @RabbitListener(queues = "${app.rabbitmq.stock-reserved-queue}")
    public void handleStockReserved(StockReservedEvent event) {
        this.processSagaEvent(event.orderId(), "StockReservedEvent", 
            order -> {
                order.handleStockReserved();
                
                // Si la orden está CONFIRMED (tanto pago como stock fueron exitosos), publicamos el evento final.
                if (order.getStatus().toString().equals("CONFIRMED")) {
                    return eventPublisherPort.publishOrderConfirmedEvent(
                        new com.store.common.events.OrderConfirmedEvent(order.getOrderId(), order.getClientId())
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
                
                // No se necesita rollback a Pagos ya que el pago no se hizo (o se revirtió antes),
                // y no se necesita rollback a Stock porque el fallo ocurrió ahí. 
                // Solo se necesita notificación de cancelación.
                return eventPublisherPort.publishOrderCancelledEvent(
                    new com.store.common.events.OrderCancelledEvent(order.getOrderId(), order.getClientId(), event.reason())
                );
            }
        ).subscribeOn(Schedulers.boundedElastic()).subscribe();
    }

    /**
     * Helper method to centralize the reactive sequence: Find -> Apply Domain Logic -> Save -> Publish Next Action.
     * This ensures the order state is saved before any subsequent event/command is published.
     */
    private Mono<Void> processSagaEvent(UUID orderId, String eventName, java.util.function.Function<Order, Mono<Void>> action) {
        return orderRepositoryPort.findById(orderId)
            .switchIfEmpty(Mono.error(new OrderNotFoundException(orderId)))
            .flatMap(order -> {
                log.info("Processing {} for Order ID: {} (Status: {})", eventName, orderId, order.getStatus());
                
                Mono<Void> publishAction = action.apply(order);
                
                Mono<Order> saveAction = orderRepositoryPort.save(order);
                
                // Aseguramos la atomicidad: Guardamos el estado de la orden y luego publicamos el siguiente evento/comando
                return saveAction.then(publishAction); 
            })
            .onErrorResume(e -> {
                log.error("Error processing Saga event {} for Order ID {}: {}", eventName, orderId, e.getMessage());
                return Mono.empty();
            });
    }
}
