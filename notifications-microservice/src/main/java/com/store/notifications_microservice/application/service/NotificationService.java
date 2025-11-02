package com.store.notifications_microservice.application.service;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.store.notifications_microservice.domain.ports.in.INotificationServicePorts;
import com.store.notifications_microservice.domain.ports.out.IBrevoSenderPorts;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationServicePorts {

    private final IBrevoSenderPorts brevoSenderPorts;

    @Override
    public Mono<Void> sendOrderConfirmation(UUID orderId, String userEmail) {
        String subject = String.format("🎉 Confirmación de Pedido #%s", orderId.toString().substring(0, 8));
        String body = String.format("""
            <h1>¡Gracias por tu compra!</h1>
            <p>Tu pedido con ID **%s** ha sido confirmado y tu pago ha sido procesado exitosamente.</p>
            <p>Pronto recibirás una notificación cuando tu pedido sea enviado.</p>
            """, orderId);
            
        return brevoSenderPorts.sendTransactionalEmail(userEmail, subject, body);
    }

    @Override
    public Mono<Void> sendPaymentFailureNotification(UUID orderId, String userEmail, String reason) {
        String subject = String.format("❌ Problema con tu Pedido #%s", orderId.toString().substring(0, 8));
        String body = String.format("""
            <h1>¡Atención! Fallo en el Pago</h1>
            <p>No pudimos procesar el pago de tu pedido **%s**.</p>
            <p>Razón: %s</p>
            <p>Por favor, revisa tus datos de pago o intenta nuevamente para evitar la cancelación del pedido.</p>
            """, orderId, reason);
            
        return brevoSenderPorts.sendTransactionalEmail(userEmail, subject, body);
    }
}