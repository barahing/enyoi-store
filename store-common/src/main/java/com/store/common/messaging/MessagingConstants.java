package com.store.common.messaging;

public final class MessagingConstants {
    private MessagingConstants() {}

    public static final String PRODUCT_EXCHANGE = "product.exchange";
    public static final String PRODUCT_CREATED_ROUTING_KEY = "product.created";
    public static final String PRODUCT_STOCK_UPDATED_ROUTING_KEY = "product.stock.updated";
    public static final String USER_EXCHANGE = "user.exchange";
    public static final String USER_CREATED_ROUTING_KEY = "user.created";
    public static final String CART_EXCHANGE = "cart.exchange";
    public static final String CART_ABANDONED_ROUTING_KEY = "cart.abandoned";
    public static final String ORDER_EXCHANGE = "order.exchange";
    public static final String ORDER_CREATED_ROUTING_KEY = "order.created";
    public static final String ORDER_STATUS_CHANGED_ROUTING_KEY = "order.status.changed";
}
