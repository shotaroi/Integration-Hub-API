package com.shotaroi.integrationhub.orders.persistence;

import com.shotaroi.integrationhub.orders.domain.OrderStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "partner_key", nullable = false)
    private String partnerKey;

    @Column(name = "external_order_id")
    private String externalOrderId;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "last_error_code")
    private String lastErrorCode;

    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    protected OrderEntity() {}

    public OrderEntity(String partnerKey, String externalOrderId, String idempotencyKey, OrderStatus status) {
        this.partnerKey = partnerKey;
        this.externalOrderId = externalOrderId;
        this.idempotencyKey = idempotencyKey;
        this.status = status;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.retryCount = 0;
    }

    public UUID getId() { return id; }
    public String getPartnerKey() { return partnerKey; }
    public String getExternalOrderId() { return externalOrderId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public OrderStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getLastErrorCode() { return lastErrorCode; }
    public String getLastErrorMessage() { return lastErrorMessage; }
    public int getRetryCount() { return retryCount; }
    public Instant getNextRetryAt() { return nextRetryAt; }

    public void setStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    public void setLastError(String code, String message) {
        this.lastErrorCode = code;
        this.lastErrorMessage = message;
        this.updatedAt = Instant.now();
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
        this.updatedAt = Instant.now();
    }

    public void setNextRetryAt(Instant nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
        this.updatedAt = Instant.now();
    }
}
