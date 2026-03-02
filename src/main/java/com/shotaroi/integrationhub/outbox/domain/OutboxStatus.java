package com.shotaroi.integrationhub.outbox.domain;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED,
    DEAD_LETTER
}
