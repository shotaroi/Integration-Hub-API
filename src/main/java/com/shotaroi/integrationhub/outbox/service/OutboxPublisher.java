package com.shotaroi.integrationhub.outbox.service;

import com.shotaroi.integrationhub.orders.domain.OrderStatus;
import com.shotaroi.integrationhub.orders.persistence.OrderRepository;
import com.shotaroi.integrationhub.outbox.domain.OutboxStatus;
import com.shotaroi.integrationhub.outbox.persistence.OutboxEventEntity;
import com.shotaroi.integrationhub.outbox.persistence.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final long BASE_DELAY_SECONDS = 30;

    private final OutboxEventRepository outboxEventRepository;
    private final OrderRepository orderRepository;

    public OutboxPublisher(OutboxEventRepository outboxEventRepository, OrderRepository orderRepository) {
        this.outboxEventRepository = outboxEventRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventEntity> events = outboxEventRepository.findPendingOrFailedReadyForRetry(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED),
                Instant.now()
        );

        for (OutboxEventEntity event : events) {
            try {
                publishEvent(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", event.getId(), e.getMessage());
            }
        }
    }

    private void publishEvent(OutboxEventEntity event) {
        // Simulate publish: log the event
        log.info("Publishing event: aggregateType={}, aggregateId={}, eventType={}, payload={}",
                event.getAggregateType(), event.getAggregateId(), event.getEventType(), event.getPayloadJson());

        // Simulate success - in production this would be Kafka/RabbitMQ publish
        event.setStatus(OutboxStatus.SENT);
        event.setSentAt(Instant.now());
        outboxEventRepository.save(event);

        // Update order status to FORWARDED when event is sent
        orderRepository.findById(event.getAggregateId()).ifPresent(order -> {
            order.setStatus(OrderStatus.FORWARDED);
            orderRepository.save(order);
        });
    }

    /**
     * Simulated failure path - for testing retry logic.
     * In production, this would be called when the actual publish fails.
     */
    @Transactional
    public void markAsFailed(OutboxEventEntity event, String error) {
        event.setAttempts(event.getAttempts() + 1);
        event.setLastError(error);

        if (event.getAttempts() >= MAX_ATTEMPTS) {
            event.setStatus(OutboxStatus.DEAD_LETTER);
            event.setNextAttemptAt(null);
            orderRepository.findById(event.getAggregateId()).ifPresent(order -> {
                order.setStatus(OrderStatus.DEAD_LETTER);
                order.setLastError("OUTBOX_DEAD_LETTER", error);
                orderRepository.save(order);
            });
        } else {
            event.setStatus(OutboxStatus.FAILED);
            long delaySeconds = (long) Math.pow(2, event.getAttempts()) * BASE_DELAY_SECONDS;
            event.setNextAttemptAt(Instant.now().plusSeconds(delaySeconds));
            orderRepository.findById(event.getAggregateId()).ifPresent(order -> {
                order.setStatus(OrderStatus.FAILED_RETRYING);
                order.setLastError("OUTBOX_FAILED", error);
                order.setRetryCount(event.getAttempts());
                order.setNextRetryAt(event.getNextAttemptAt());
                orderRepository.save(order);
            });
        }
        outboxEventRepository.save(event);
    }
}
