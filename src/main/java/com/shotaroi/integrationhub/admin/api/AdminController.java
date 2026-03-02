package com.shotaroi.integrationhub.admin.api;

import com.shotaroi.integrationhub.orders.domain.OrderStatus;
import com.shotaroi.integrationhub.orders.persistence.OrderEntity;
import com.shotaroi.integrationhub.orders.persistence.OrderRepository;
import com.shotaroi.integrationhub.outbox.domain.OutboxStatus;
import com.shotaroi.integrationhub.outbox.persistence.OutboxEventEntity;
import com.shotaroi.integrationhub.outbox.persistence.OutboxEventRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin", description = "Admin operations (basic auth required)")
public class AdminController {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;

    public AdminController(OrderRepository orderRepository, OutboxEventRepository outboxEventRepository) {
        this.orderRepository = orderRepository;
        this.outboxEventRepository = outboxEventRepository;
    }

    @GetMapping("/orders")
    @Operation(summary = "List orders by status")
    public List<AdminOrderResponse> listOrders(
            @RequestParam(required = false) List<OrderStatus> status
    ) {
        List<OrderStatus> statuses = status != null && !status.isEmpty()
                ? status
                : List.of(OrderStatus.FAILED_RETRYING, OrderStatus.DEAD_LETTER, OrderStatus.FORWARDED);

        return orderRepository.findByStatusIn(statuses).stream()
                .map(this::toResponse)
                .toList();
    }

    @PostMapping("/orders/{id}/retry")
    @Operation(summary = "Retry order outbox publishing")
    public ResponseEntity<AdminOrderResponse> retryOrder(@PathVariable UUID id) {
        Optional<OrderEntity> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        OrderEntity order = orderOpt.get();
        order.setStatus(OrderStatus.FORWARDING);
        order.setRetryCount(0);
        order.setNextRetryAt(null);
        order.setLastError(null, null);
        orderRepository.save(order);

        outboxEventRepository.findByAggregateId(id).stream()
                .filter(e -> e.getStatus() == OutboxStatus.PENDING || e.getStatus() == OutboxStatus.FAILED)
                .forEach(e -> {
                    e.setStatus(OutboxStatus.PENDING);
                    e.setAttempts(0);
                    e.setNextAttemptAt(null);
                    e.setLastError(null);
                    outboxEventRepository.save(e);
                });

        return ResponseEntity.ok(toResponse(order));
    }

    @GetMapping("/outbox")
    @Operation(summary = "List outbox events by status")
    public List<AdminOutboxResponse> listOutbox(
            @RequestParam(required = false) List<OutboxStatus> status
    ) {
        if (status != null && !status.isEmpty()) {
            return status.stream()
                    .flatMap(s -> outboxEventRepository.findByStatus(s).stream())
                    .map(this::toOutboxResponse)
                    .toList();
        }
        return outboxEventRepository.findAll().stream()
                .map(this::toOutboxResponse)
                .toList();
    }

    private AdminOrderResponse toResponse(OrderEntity o) {
        return new AdminOrderResponse(
                o.getId(),
                o.getPartnerKey(),
                o.getExternalOrderId(),
                o.getIdempotencyKey(),
                o.getStatus().name(),
                o.getCreatedAt(),
                o.getUpdatedAt(),
                o.getRetryCount(),
                o.getNextRetryAt(),
                o.getLastErrorCode(),
                o.getLastErrorMessage()
        );
    }

    private AdminOutboxResponse toOutboxResponse(OutboxEventEntity e) {
        return new AdminOutboxResponse(
                e.getId(),
                e.getAggregateType(),
                e.getAggregateId(),
                e.getEventType(),
                e.getStatus().name(),
                e.getAttempts(),
                e.getNextAttemptAt(),
                e.getCreatedAt(),
                e.getSentAt(),
                e.getLastError()
        );
    }

    public record AdminOrderResponse(
            UUID id,
            String partnerKey,
            String externalOrderId,
            String idempotencyKey,
            String status,
            Instant createdAt,
            Instant updatedAt,
            int retryCount,
            Instant nextRetryAt,
            String lastErrorCode,
            String lastErrorMessage
    ) {}

    public record AdminOutboxResponse(
            UUID id,
            String aggregateType,
            UUID aggregateId,
            String eventType,
            String status,
            int attempts,
            Instant nextAttemptAt,
            Instant createdAt,
            Instant sentAt,
            String lastError
    ) {}
}
