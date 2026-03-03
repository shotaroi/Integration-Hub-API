package com.shotaroi.integrationhub.partner;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "partners")
public class PartnerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "partner_key", unique = true, nullable = false)
    private String partnerKey;

    @Column(name = "api_key_hash", nullable = false)
    private String apiKeyHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Default constructor for JPA
    protected PartnerEntity() {}

    public PartnerEntity(String partnerKey, String apiKeyHash) {
        this.partnerKey = partnerKey;
        this.apiKeyHash = apiKeyHash;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getPartnerKey() { return partnerKey; }
    public String getApiKeyHash() { return apiKeyHash; }
    public Instant getCreatedAt() { return createdAt; }
}
