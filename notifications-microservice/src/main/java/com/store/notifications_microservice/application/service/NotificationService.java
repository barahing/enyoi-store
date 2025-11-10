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
    public Mono<Void> sendPendingPayment(UUID orderId, String userEmail) {
        String subject = String.format("🕒 Pedido #%s pendiente de pago", orderId.toString().substring(0, 8));
        String body = String.format("""
            <h1>Tu pedido está pendiente de pago</h1>
            <p>El pedido con ID <strong>%s</strong> ha sido creado exitosamente.</p>
            <p>Completa el pago para que podamos procesarlo.</p>
            <p><i>Gracias por tu preferencia.</i></p>
            """, orderId);
        return brevoSenderPorts.sendTransactionalEmail(userEmail, subject, body);
    }

    @Override
    public Mono<Void> sendOrderConfirmation(UUID orderId, String userEmail) {
        String subject = String.format("🎉 Confirmación de Pedido #%s", orderId.toString().substring(0, 8));
        String body = String.format("""
            <h1>¡Gracias por tu compra!</h1>
            <p>Tu pedido con ID <strong>%s</strong> ha sido confirmado y tu pago fue procesado exitosamente.</p>
            <p>Pronto recibirás una notificación cuando tu pedido sea enviado.</p>
            """, orderId);
        return brevoSenderPorts.sendTransactionalEmail(userEmail, subject, body);
    }

    @Override
    public Mono<Void> sendPaymentFailureNotification(UUID orderId, String userEmail, String reason) {
        String subject = String.format("❌ Problema con tu Pedido #%s", orderId.toString().substring(0, 8));
        String body = String.format("""
            <h1>Fallo en el Pago</h1>
            <p>No pudimos procesar el pago de tu pedido <strong>%s</strong>.</p>
            <p><b>Razón:</b> %s</p>
            <p>Por favor revisa tus datos de pago o intenta nuevamente.</p>
            """, orderId, reason);
        return brevoSenderPorts.sendTransactionalEmail(userEmail, subject, body);
    }

    @Override
    public Mono<Void> sendOrderShipped(UUID orderId, String userEmail) {
        String subject = String.format("🚚 Tu pedido #%s ha sido enviado", orderId.toString().substring(0, 8));
        String body = String.format("""
            <h1>Tu pedido está en camino</h1>
            <p>El pedido <strong>%s</strong> fue despachado y se encuentra en tránsito.</p>
            <p>Pronto recibirás la confirmación de entrega.</p>
            """, orderId);
        return brevoSenderPorts.sendTransactionalEmail(userEmail, subject, body);
    }

    @Override
    public Mono<Void> sendOrderDelivered(UUID orderId, String userEmail) {
        String subject = String.format("📦 Pedido #%s entregado", orderId.toString().substring(0, 8));
        String body = String.format("""
            <h1>¡Tu pedido ha sido entregado!</h1>
            <p>El pedido <strong>%s</strong> ha sido recibido satisfactoriamente.</p>
            <p>Esperamos que disfrutes tu compra. ¡Gracias por elegirnos!</p>
            """, orderId);
        return brevoSenderPorts.sendTransactionalEmail(userEmail, subject, body);
    }

    @Override
    public Mono<Void> sendUserCreated(String email, String firstName, String lastName) {
        String subject = "🎉 ¡Bienvenido/a a Arka Store!";
        String body = String.format("""
            <h1>¡Hola, %s %s!</h1>
            <p>Tu cuenta ha sido creada exitosamente.</p>
            <p>Ya puedes iniciar sesión y comenzar a comprar.</p>
            <p>¡Gracias por unirte a Arka Store!</p>
            """, firstName, lastName);
        return brevoSenderPorts.sendTransactionalEmail(email, subject, body);
    }
}
