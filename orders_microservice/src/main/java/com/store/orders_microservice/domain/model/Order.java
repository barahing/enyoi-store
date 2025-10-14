package com.store.orders_microservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.store.orders_microservice.domain.exception.OrderCancelledException;
import com.store.orders_microservice.domain.exception.OrderCannotBeConfirmedException;
import com.store.orders_microservice.domain.exception.OrderCannotBeDeliveredException;
import com.store.orders_microservice.domain.exception.OrderCannotBeModifiedException;
import com.store.orders_microservice.domain.exception.OrderCannotBeShippedException;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order {

    private UUID orderId;
    private UUID clientId;
    private List<OrderItem> items;
    private BigDecimal total;
    private OrderStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public void confirm() {
        if (this.status != OrderStatus.CREATED)
            throw new OrderCannotBeConfirmedException(this.orderId, this.status);
        this.status = OrderStatus.CONFIRMED;
        markUpdated();
    }

    public void ship() {
        if (this.status != OrderStatus.CONFIRMED)
            throw new OrderCannotBeShippedException(this.orderId, status);
        this.status = OrderStatus.SHIPPED;
        markUpdated();
    }

    public void deliver() {
        if (this.status != OrderStatus.SHIPPED)
            throw new OrderCannotBeDeliveredException(this.orderId, status);
        this.status = OrderStatus.DELIVERED;
        markUpdated();
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELLED)
            throw new OrderCancelledException(orderId, status);
        if (this.status != OrderStatus.CREATED)
            throw new OrderCannotBeModifiedException(this.orderId, status);
        this.status = OrderStatus.CANCELLED;
        markUpdated();
    }

    public void addItem(OrderItem newItem) {
        if (!isModifiable())
            throw new OrderCannotBeModifiedException(this.orderId, status);

        for (int i = 0; i < items.size(); i++) {
            OrderItem existing = items.get(i);
            if (existing.productId().equals(newItem.productId())) {
                int updatedQty = existing.quantity() + newItem.quantity();
                items.set(i, existing.withUpdatedQuantity(updatedQty));
                recalculateTotal();
                markUpdated();
                return;
            }
        }
        items.add(newItem);
        recalculateTotal();
        markUpdated();
    }

    public void removeItem(UUID productId) {
        if (!isModifiable())
            throw new OrderCannotBeModifiedException(this.orderId, this.status);

        items.removeIf(item -> item.productId().equals(productId));
        recalculateTotal();
        markUpdated();
    }

    public void updateItemQuantity(UUID productId, int newQuantity) {
        if (!isModifiable())
            throw new OrderCannotBeModifiedException(this.orderId, this.status);

        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            if (item.productId().equals(productId)) {
                items.set(i, item.withUpdatedQuantity(newQuantity));
                recalculateTotal();
                markUpdated();
                return;
            }
        }
        throw new IllegalArgumentException("Product not found in order: " + productId);
    }

    public void recalculateTotal() {
        if (items == null || items.isEmpty()) {
            this.total = BigDecimal.ZERO;
            return;
        }
        this.total = items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isModifiable() {
        return this.status == OrderStatus.CREATED;
    }

    public void markUpdated() {
        this.updatedDate = LocalDateTime.now();
    }
}
