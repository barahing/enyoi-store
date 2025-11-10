package com.store.notifications_microservice.infrastructure.messaging.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.store.notifications_microservice.domain.ports.in.INotificationServicePorts;
import com.store.common.events.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventsConsumer {

    private final INotificationServicePorts notificationServicePorts;
    private static final String TEST_EMAIL = "rikbarahona@gmail.com";

    // 🎯 1️⃣ Order Created → Pending payment
    @RabbitListener(id = "orderCreatedListener", queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("📩 [RECEIVED] OrderCreatedEvent | orderId={} | amount={} | userId={}",
                event.getOrderId(), event.getAmount(), event.getUserId());

        notificationServicePorts.sendPendingPayment(event.getOrderId(), TEST_EMAIL)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSubscribe(s -> log.info("➡️  [ACTION] PendingPayment email | orderId={}", event.getOrderId()))
            .doOnSuccess(v -> log.info("✅ [SENT] PendingPayment email | orderId={}", event.getOrderId()))
            .doOnError(e -> log.error("❌ [ERROR] PendingPayment email | orderId={} | reason={}",
                    event.getOrderId(), e.getMessage()))
            .subscribe();
    }

    // 🎯 2️⃣ Order Confirmed → Payment approved
    @RabbitListener(id = "orderConfirmedListener", queues = "order.confirmed.queue")
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        log.info("📩 [RECEIVED] OrderConfirmedEvent | orderId={} | userId={}",
                event.getOrderId(), event.getUserId());

        notificationServicePorts.sendOrderConfirmation(event.getOrderId(), TEST_EMAIL)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSubscribe(s -> log.info("➡️  [ACTION] OrderConfirmation email | orderId={}", event.getOrderId()))
            .doOnSuccess(v -> log.info("✅ [SENT] OrderConfirmation email | orderId={}", event.getOrderId()))
            .doOnError(e -> log.error("❌ [ERROR] OrderConfirmation email | orderId={} | reason={}",
                    event.getOrderId(), e.getMessage()))
            .subscribe();
    }

    // 🎯 3️⃣ Payment Failed
    @RabbitListener(id = "paymentFailedListener", queues = "payment.failed.queue")
    public void handlePaymentFailed(PaymentFailedEvent event) {
        log.info("📩 [RECEIVED] PaymentFailedEvent | orderId={} | reason={}",
                event.getOrderId(), event.getReason());

        notificationServicePorts.sendPaymentFailureNotification(event.getOrderId(), TEST_EMAIL, event.getReason())
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSubscribe(s -> log.info("➡️  [ACTION] PaymentFailure email | orderId={}", event.getOrderId()))
            .doOnSuccess(v -> log.info("✅ [SENT] PaymentFailure email | orderId={}", event.getOrderId()))
            .doOnError(e -> log.error("❌ [ERROR] PaymentFailure email | orderId={} | reason={}",
                    event.getOrderId(), e.getMessage()))
            .subscribe();
    }

    // 🎯 4️⃣ Order Shipped
    @RabbitListener(id = "orderShippedListener", queues = "order.shipped.queue")
    public void handleOrderShipped(OrderShippedEvent event) {
        log.info("📩 [RECEIVED] OrderShippedEvent | orderId={} | trackingCode={}",
                event.getOrderId(), event.getTrackingCode());

        notificationServicePorts.sendOrderShipped(event.getOrderId(), TEST_EMAIL)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSubscribe(s -> log.info("➡️  [ACTION] OrderShipped email | orderId={}", event.getOrderId()))
            .doOnSuccess(v -> log.info("✅ [SENT] OrderShipped email | orderId={}", event.getOrderId()))
            .doOnError(e -> log.error("❌ [ERROR] OrderShipped email | orderId={} | reason={}",
                    event.getOrderId(), e.getMessage()))
            .subscribe();
    }

    // 🎯 5️⃣ Order Delivered
    @RabbitListener(id = "orderDeliveredListener", queues = "order.delivered.queue")
    public void handleOrderDelivered(OrderDeliveredEvent event) {
        log.info("📩 [RECEIVED] OrderDeliveredEvent | orderId={} | deliveryDate={}",
                event.getOrderId(), event.getDeliveryDate());

        notificationServicePorts.sendOrderDelivered(event.getOrderId(), TEST_EMAIL)
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSubscribe(s -> log.info("➡️  [ACTION] OrderDelivered email | orderId={}", event.getOrderId()))
            .doOnSuccess(v -> log.info("✅ [SENT] OrderDelivered email | orderId={}", event.getOrderId()))
            .doOnError(e -> log.error("❌ [ERROR] OrderDelivered email | orderId={} | reason={}",
                    event.getOrderId(), e.getMessage()))
            .subscribe();
    }

    // 🎯 6️⃣ User Created
    @RabbitListener(id = "userCreatedListener", queues = "user.created.queue")
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("📩 [RECEIVED] UserCreatedEvent | userId={} | email={} | name={} {} | role={}",
                event.userId(), event.email(), event.firstName(), event.lastName(), event.role());

        notificationServicePorts.sendUserCreated(event.email(), event.firstName(), event.lastName())
            .subscribeOn(Schedulers.boundedElastic())
            .doOnSubscribe(s -> log.info("➡️  [ACTION] Welcome email | email={}", event.email()))
            .doOnSuccess(v -> log.info("✅ [SENT] Welcome email | email={}", event.email()))
            .doOnError(e -> log.error("❌ [ERROR] Welcome email | email={} | reason={}",
                    event.email(), e.getMessage()))
            .subscribe();
    }
}
