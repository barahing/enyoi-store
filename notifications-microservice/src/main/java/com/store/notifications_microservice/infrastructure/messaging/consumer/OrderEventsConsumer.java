package com.store.notifications_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.store.notifications_microservice.domain.ports.in.INotificationServicePorts;
import com.store.common.events.OrderConfirmedEvent;
import com.store.common.events.PaymentFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventsConsumer {

    private final INotificationServicePorts notificationServicePorts;
    
    // Este email debería ser obtenido del evento, pero lo mantenemos fijo para la simulación.
    private static final String DUMMY_EMAIL = "rikbarahona@gmail.com"; 

    @RabbitListener(queues = "${app.rabbitmq.notification-queue}")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received OrderConfirmedEvent for Order ID: {}", event.getOrderId());
        
        notificationServicePorts.sendOrderConfirmation(event.getOrderId(), DUMMY_EMAIL)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }

    @RabbitListener(queues = "${app.rabbitmq.notification-queue}")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("Received PaymentFailedEvent for Order ID: {}", event.getOrderId());
        
        notificationServicePorts.sendPaymentFailureNotification(event.getOrderId(), DUMMY_EMAIL, event.getReason())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();
    }
}