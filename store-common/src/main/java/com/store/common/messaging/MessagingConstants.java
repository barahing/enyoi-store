package com.store.common.messaging;

public final class MessagingConstants {
    private MessagingConstants() {}

    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String CART_EXCHANGE = "cart.exchange";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String INVENTORY_EXCHANGE = "inventory.exchange";
    public static final String SHIPPING_EXCHANGE = "shipping.exchange";

    public static final String USER_CREATED_ROUTING_KEY = "user.created";
    public static final String USER_DEACTIVATED_ROUTING_KEY = "user.deactivated";
    public static final String USER_ACTIVATED_ROUTING_KEY = "user.activated";

    public static final String PRODUCT_CREATED_ROUTING_KEY = "product.created";
    public static final String PRODUCT_STOCK_UPDATED_ROUTING_KEY = "product.stock.updated";

    public static final String CART_ABANDONED_ROUTING_KEY = "cart.abandoned";
    public static final String CART_CONVERTED_ROUTING_KEY = "cart.converted";

    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_CONFIRMED_ROUTING_KEY = "order.confirmed";
    public static final String ORDER_CANCELLED_ROUTING_KEY = "order.cancelled";
    public static final String ORDER_STATUS_CHANGED_ROUTING_KEY = "order.status.changed";

    public static final String PAYMENT_PROCESSED_ROUTING_KEY = "payment.processed";
    public static final String PAYMENT_FAILED_ROUTING_KEY = "payment.failed";
    public static final String PROCESS_PAYMENT_COMMAND_ROUTING_KEY = "payment.process.command";

    public static final String STOCK_RESERVED_ROUTING_KEY = "stock.reserved";
    public static final String STOCK_RESERVATION_FAILED_ROUTING_KEY = "stock.reservation.failed";
    public static final String STOCK_RECEIVED_ROUTING_KEY = "stock.received";
    public static final String LOW_STOCK_ALERT_ROUTING_KEY = "stock.low.alert";
    public static final String RELEASE_STOCK_COMMAND_ROUTING_KEY = "stock.release.command";
    public static final String RESERVE_STOCK_COMMAND_ROUTING_KEY = "stock.reserve.command";

    public static final String SHIPPING_SENT_ROUTING_KEY = "shipping.sent";

    public static final String USER_CREATED_QUEUE = "user.created.queue";
    public static final String USER_DEACTIVATED_QUEUE = "user.deactivated.queue";
    public static final String USER_ACTIVATED_QUEUE = "user.activated.queue";

    public static final String CART_CONVERTED_QUEUE = "cart.converted.queue";

    public static final String ORDER_CREATED_QUEUE = "order.created.queue";
    public static final String ORDER_CONFIRMED_QUEUE = "order.confirmed.queue";
    public static final String ORDER_CANCELLED_QUEUE = "order.cancelled.queue";

    public static final String PAYMENT_PROCESSED_QUEUE = "payment.processed.queue";
    public static final String PAYMENT_FAILED_QUEUE = "payment.failed.queue";

    public static final String STOCK_RESERVED_QUEUE = "stock.reserved.queue";
    public static final String STOCK_RESERVATION_FAILED_QUEUE = "stock.reservation.failed.queue";

    public static final String SHIPPING_SENT_QUEUE = "shipping.sent.queue";
}