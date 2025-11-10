package com.store.notifications_microservice.domain.ports.in;

import java.util.UUID;
import reactor.core.publisher.Mono;

public interface INotificationServicePorts {
    
    Mono<Void> sendPendingPayment(UUID orderId, String userEmail);
    Mono<Void> sendOrderConfirmation(UUID orderId, String userEmail);
    Mono<Void> sendPaymentFailureNotification(UUID orderId, String userEmail, String reason);
    Mono<Void> sendOrderShipped(UUID orderId, String userEmail);
    Mono<Void> sendOrderDelivered(UUID orderId, String userEmail);
    Mono<Void> sendUserCreated(String email, String firstName, String lastName);

}