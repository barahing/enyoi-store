package com.store.carts_microservice.domain.factory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.store.carts_microservice.domain.exception.CartCreationException;
import com.store.carts_microservice.domain.exception.CartCannotBeModifiedException;
import com.store.carts_microservice.domain.model.Cart;
import com.store.carts_microservice.domain.model.CartItem;
import com.store.carts_microservice.domain.model.CartStatus;

public final class CartFactory {

    private CartFactory() {}

    public static Cart createNew(UUID clientId, List<CartItem> items) {
        if (clientId == null)
            throw CartCreationException.missingClientId();
        if (items == null || items.isEmpty())
            throw CartCreationException.emptyItems();

        LocalDateTime now = LocalDateTime.now();

        Cart cart = new Cart();
        cart.setClientId(clientId);
        cart.setItems(items);
        cart.recalculateTotal();
        cart.setStatus(CartStatus.ACTIVE);
        cart.setCreatedDate(now);
        cart.setUpdatedDate(now);

        return cart;
    }

    public static Cart modifyExisting(Cart existingCart, Cart newData) {
        if (!existingCart.isConvertible()) {
            throw new CartCannotBeModifiedException(existingCart.getCartId(), existingCart.getStatus());
        }

        existingCart.setClientId(newData.getClientId());
        existingCart.setItems(newData.getItems());
        existingCart.recalculateTotal();
        existingCart.markUpdated();

        return existingCart;
    }
}
