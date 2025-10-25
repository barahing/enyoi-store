package com.store.carts_microservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Cart {
    private UUID cartId;
    private UUID clientId;
    private List<CartItem> items;
    private BigDecimal total;
    private CartStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

    public Cart(UUID cartId, UUID clientId, List<CartItem> items, BigDecimal total, CartStatus status, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.cartId = cartId;
        this.clientId = clientId;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.total = total;
        this.status = status;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public void addItem(CartItem item) {
        if (this.items == null) throw new IllegalStateException("Cart not initialized");
        this.items.add(item);
        recalculateTotal();
        markUpdated();
    }

    public void removeItem(UUID productId) {
        this.items.removeIf(i -> i.productId().equals(productId));
        recalculateTotal();
        markUpdated();
    }

    public void updateItemQuantity(UUID productId, int newQuantity) {
        if (this.items == null) throw new IllegalStateException("Cart not initialized");
        if (newQuantity <= 0) {
             removeItem(productId);
             return;
        }

        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item.productId().equals(productId)) {
                items.set(i, item.withUpdatedQuantity(newQuantity));
                recalculateTotal();
                markUpdated();
                return;
            }
        }
        throw new IllegalArgumentException("Product not found in cart: " + productId);
    }
    
    public void recalculateTotal() {
        this.total = items.stream()
                .map(CartItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markUpdated() {
        this.updatedDate = LocalDateTime.now();
    }

    public void markAsAbandoned() {
        this.status = CartStatus.ABANDONED;
        markUpdated();
    }

    public void markAsConverted() {
        this.status = CartStatus.CONVERTED_TO_ORDER;
        markUpdated();
    }

    public boolean isConvertible() {
        return this.status == CartStatus.ACTIVE;
    }
}