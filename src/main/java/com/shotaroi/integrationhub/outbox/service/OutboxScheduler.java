package com.shotaroi.integrationhub.outbox.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxScheduler {

    private final OutboxPublisher outboxPublisher;

    public OutboxScheduler(OutboxPublisher outboxPublisher) {
        this.outboxPublisher = outboxPublisher;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishOutboxEvents() {
        outboxPublisher.publishPendingEvents();
    }
}
