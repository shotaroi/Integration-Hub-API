package com.shotaroi.integrationhub.orders.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotaroi.integrationhub.orders.domain.CanonicalCreateOrderCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartnerAMapperTest {

    private PartnerAMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mapper = new PartnerAMapper();
        objectMapper = new ObjectMapper();
    }

    @Test
    void mapsValidPartnerAPayload() throws Exception {
        String json = """
                {
                  "partnerOrderId": "A-1001",
                  "customerEmail": "a@example.com",
                  "items": [{"sku":"SKU1","qty":2,"unitPrice":199.00}],
                  "currency": "SEK"
                }
                """;
        JsonNode body = objectMapper.readTree(json);

        CanonicalCreateOrderCommand result = mapper.map(body);

        assertThat(result.externalOrderId()).isEqualTo("A-1001");
        assertThat(result.customerEmail()).isEqualTo("a@example.com");
        assertThat(result.currency()).isEqualTo("SEK");
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).sku()).isEqualTo("SKU1");
        assertThat(result.items().get(0).qty()).isEqualTo(2);
        assertThat(result.items().get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("199.00"));
    }

    @Test
    void throwsOnMissingPartnerOrderId() throws Exception {
        String json = """
                {
                  "customerEmail": "a@example.com",
                  "items": [],
                  "currency": "SEK"
                }
                """;
        JsonNode body = objectMapper.readTree(json);

        assertThatThrownBy(() -> mapper.map(body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("partnerOrderId");
    }

    @Test
    void throwsOnInvalidItems() throws Exception {
        String json = """
                {
                  "partnerOrderId": "A-1001",
                  "customerEmail": "a@example.com",
                  "currency": "SEK"
                }
                """;
        JsonNode body = objectMapper.readTree(json);

        assertThatThrownBy(() -> mapper.map(body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("items");
    }
}
