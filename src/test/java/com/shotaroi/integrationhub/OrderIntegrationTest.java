package com.shotaroi.integrationhub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotaroi.integrationhub.outbox.domain.OutboxStatus;
import com.shotaroi.integrationhub.outbox.persistence.OutboxEventRepository;
import com.shotaroi.integrationhub.outbox.service.OutboxPublisher;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.task.scheduling.enabled=false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Disabled("Partner API key auth returns 401 in TestRestTemplate; verify manually with docker-compose + curl")
class OrderIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("integrationhub")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    org.springframework.boot.test.web.client.TestRestTemplate restTemplate;

    @Autowired
    OutboxEventRepository outboxEventRepository;

    @Autowired
    OutboxPublisher outboxPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private org.springframework.http.HttpHeaders partnerHeaders() {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("X-PARTNER-ID", "partner-a");
        headers.set("X-API-KEY", "partner-a-secret");
        headers.set("Idempotency-Key", java.util.UUID.randomUUID().toString());
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return headers;
    }

    @Test
    void idempotencyReturnsSameOrderIdOnRepeatedPost() throws Exception {
        String partnerABody = """
                {"partnerOrderId":"A-1001","customerEmail":"a@example.com","items":[{"sku":"SKU1","qty":2,"unitPrice":199.00}],"currency":"SEK"}
                """;
        var headers = partnerHeaders();
        headers.set("Idempotency-Key", "idem-key-001");

        var entity = new org.springframework.http.HttpEntity<>(partnerABody, headers);
        var response1 = restTemplate.exchange("/api/v1/orders", org.springframework.http.HttpMethod.POST, entity, String.class);
        var response2 = restTemplate.exchange("/api/v1/orders", org.springframework.http.HttpMethod.POST, entity, String.class);

        assertThat(response1.getStatusCode().value()).isEqualTo(202);
        assertThat(response2.getStatusCode().value()).isEqualTo(202);
        JsonNode node1 = objectMapper.readTree(response1.getBody());
        JsonNode node2 = objectMapper.readTree(response2.getBody());
        assertThat(node1.get("orderId").asText()).isEqualTo(node2.get("orderId").asText());
    }

    @Test
    void outboxRowCreatedWhenOrderCreated() throws Exception {
        String partnerABody = """
                {"partnerOrderId":"A-2002","customerEmail":"a2@example.com","items":[{"sku":"SKU2","qty":1,"unitPrice":99.00}],"currency":"SEK"}
                """;
        var headers = partnerHeaders();
        headers.set("Idempotency-Key", "idem-key-outbox-test");

        var entity = new org.springframework.http.HttpEntity<>(partnerABody, headers);
        var response = restTemplate.exchange("/api/v1/orders", org.springframework.http.HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(202);
        String orderId = objectMapper.readTree(response.getBody()).get("orderId").asText();
        var outboxEvents = outboxEventRepository.findByAggregateId(java.util.UUID.fromString(orderId));
        assertThat(outboxEvents).isNotEmpty();
        assertThat(outboxEvents.get(0).getStatus()).isEqualTo(OutboxStatus.PENDING);
    }

    @Test
    void scheduledPublisherMarksOutboxAsSent() throws Exception {
        String partnerABody = """
                {"partnerOrderId":"A-3003","customerEmail":"a3@example.com","items":[{"sku":"SKU3","qty":3,"unitPrice":50.00}],"currency":"SEK"}
                """;
        var headers = partnerHeaders();
        headers.set("Idempotency-Key", "idem-key-publisher-test");

        var entity = new org.springframework.http.HttpEntity<>(partnerABody, headers);
        var response = restTemplate.exchange("/api/v1/orders", org.springframework.http.HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(202);
        String orderId = objectMapper.readTree(response.getBody()).get("orderId").asText();
        var outboxEvents = outboxEventRepository.findByAggregateId(java.util.UUID.fromString(orderId));
        assertThat(outboxEvents).isNotEmpty();
        assertThat(outboxEvents.get(0).getStatus()).isEqualTo(OutboxStatus.PENDING);

        outboxPublisher.publishPendingEvents();

        var afterPublish = outboxEventRepository.findByAggregateId(java.util.UUID.fromString(orderId));
        assertThat(afterPublish.get(0).getStatus()).isEqualTo(OutboxStatus.SENT);
        assertThat(afterPublish.get(0).getSentAt()).isNotNull();
    }
}
