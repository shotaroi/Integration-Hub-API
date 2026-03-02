package com.shotaroi.integrationhub.orders.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_payloads")
public class OrderPayloadEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> rawJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "normalized_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> normalizedJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrderPayloadEntity() {}

    public OrderPayloadEntity(OrderEntity order, Map<String, Object> rawJson, Map<String, Object> normalizedJson) {
        this.order = order;
        this.rawJson = rawJson;
        this.normalizedJson = normalizedJson;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public OrderEntity getOrder() { return order; }
    public Map<String, Object> getRawJson() { return rawJson; }
    public Map<String, Object> getNormalizedJson() { return normalizedJson; }
    public Instant getCreatedAt() { return createdAt; }
}
