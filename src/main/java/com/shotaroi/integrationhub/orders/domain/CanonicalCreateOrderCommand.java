package com.shotaroi.integrationhub.orders.domain;

import java.math.BigDecimal;
import java.util.List;

public record CanonicalCreateOrderCommand(
        String externalOrderId,
        String customerEmail,
        List<OrderItem> items,
        String currency
) {
    public record OrderItem(String sku, int qty, BigDecimal unitPrice) {}
}
