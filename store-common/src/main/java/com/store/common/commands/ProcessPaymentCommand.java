package com.store.common.commands;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessPaymentCommand(
    UUID orderId,
    UUID clientId,
    BigDecimal amount,
    String paymentMethod
) {

}