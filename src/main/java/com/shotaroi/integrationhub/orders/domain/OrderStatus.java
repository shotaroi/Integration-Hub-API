package com.shotaroi.integrationhub.orders.domain;

public enum OrderStatus {
    RECEIVED,
    VALIDATION_FAILED,
    VALIDATED,
    FORWARDING,
    FORWARDED,
    FAILED_RETRYING,
    DEAD_LETTER
}
