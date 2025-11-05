package com.store.carts_microservice.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    // Constructor para la Factory - mantiene inmutabilidad en creación
    public Cart(UUID cartId, UUID clientId, List<CartItem> items, BigDecimal total, 
                CartStatus status, LocalDateTime createdDate, LocalDateTime updatedDate) {
        this.cartId = cartId;
        this.clientId = clientId;
        this.items = new ArrayList<>(items); // Copia defensiva
        this.total = total;
        this.status = status;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    // ✅ NUEVO: Método para asignar items después de cargar desde BD
    public void setItems(List<CartItem> items) {
        this.items = new ArrayList<>(items);
        recalculateTotal();
    }

    public void addItem(CartItem newItem) {
        Optional<CartItem> existingItem = items.stream()
            .filter(item -> item.productId().equals(newItem.productId()))
            .findFirst();

        if (existingItem.isPresent()) {
            CartItem oldItem = existingItem.get();
            int newQuantity = oldItem.quantity() + newItem.quantity();
            
            CartItem updatedItem = new CartItem(
                oldItem.productId(), 
                newQuantity, 
                oldItem.price(), 
                oldItem.price().multiply(BigDecimal.valueOf(newQuantity))
            );
            
            items.remove(oldItem);
            items.add(updatedItem);
        } else {
            items.add(newItem);
        }
        recalculateTotal();
        this.updatedDate = LocalDateTime.now();
    }

    public void updateItemQuantity(UUID productId, int newQuantity) {
        if (newQuantity <= 0) {
            removeItem(productId);
            return;
        }

        Optional<CartItem> existingItem = items.stream()
            .filter(item -> item.productId().equals(productId))
            .findFirst();

        existingItem.ifPresent(oldItem -> {
            CartItem updatedItem = new CartItem(
                oldItem.productId(), 
                newQuantity, 
                oldItem.price(), 
                oldItem.price().multiply(BigDecimal.valueOf(newQuantity))
            );
            items.remove(oldItem);
            items.add(updatedItem);
            recalculateTotal();
            this.updatedDate = LocalDateTime.now();
        });
    }

    public void removeItem(UUID productId) {
        boolean removed = items.removeIf(item -> item.productId().equals(productId));
        if (removed) {
            recalculateTotal();
            this.updatedDate = LocalDateTime.now();
        }
    }

    private void recalculateTotal() {
        this.total = items.stream()
            .map(CartItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void markAsConverting() {
        if (this.status != CartStatus.ACTIVE) {
            throw new IllegalStateException("Only ACTIVE carts can start conversion.");
        }
        this.status = CartStatus.CONVERTING;
        this.updatedDate = LocalDateTime.now();
    }

    public void markAsConverted() {
        if (this.status != CartStatus.ACTIVE && this.status != CartStatus.CONVERTING) {
            throw new IllegalStateException("Only ACTIVE or CONVERTING carts can be converted to orders.");
        }
        this.status = CartStatus.CONVERTED_TO_ORDER;
        this.updatedDate = LocalDateTime.now();
    }

    public boolean isConvertible() {
        return this.status == CartStatus.ACTIVE && !items.isEmpty();
    }
}