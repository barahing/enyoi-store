package com.store.orders_microservice.domain.factory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.store.orders_microservice.domain.exception.OrderCannotBeModifiedException;
import com.store.orders_microservice.domain.exception.OrderCreationException;
import com.store.orders_microservice.domain.model.Order;
import com.store.orders_microservice.domain.model.OrderItem;
import com.store.orders_microservice.domain.model.OrderStatus;

public final class OrderFactory {

    private OrderFactory() {}

    public static Order createNew(UUID clientId, List<OrderItem> items) {
        if (clientId == null)
            throw OrderCreationException.missingClientId();
        if (items == null || items.isEmpty())
            throw OrderCreationException.emptyItems();

        LocalDateTime now = LocalDateTime.now();

        Order order = new Order();
        order.setClientId(clientId);
        order.setItems(items);
        order.recalculateTotal();
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedDate(now);
        order.setUpdatedDate(now);

        return order;
    }

    public static Order modifyExisting(Order existingOrder, Order newData) {
        if (!existingOrder.isModifiable())
            throw new OrderCannotBeModifiedException(existingOrder.getOrderId(), existingOrder.getStatus());

        existingOrder.setClientId(newData.getClientId());
        existingOrder.setItems(newData.getItems());
        existingOrder.recalculateTotal();
        existingOrder.markUpdated();

        return existingOrder;
    }
}
