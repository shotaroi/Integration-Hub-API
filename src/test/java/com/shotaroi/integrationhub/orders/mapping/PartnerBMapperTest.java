package com.shotaroi.integrationhub.orders.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotaroi.integrationhub.orders.domain.CanonicalCreateOrderCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartnerBMapperTest {

    private PartnerBMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mapper = new PartnerBMapper();
        objectMapper = new ObjectMapper();
    }

    @Test
    void mapsValidPartnerBPayload() throws Exception {
        String json = """
                {
                  "orderRef": "B-9009",
                  "buyer": {"email":"b@example.com"},
                  "lines": [{"productCode":"SKU1","amount":2,"price":199.00}],
                  "currencyCode":"SEK"
                }
                """;
        JsonNode body = objectMapper.readTree(json);

        CanonicalCreateOrderCommand result = mapper.map(body);

        assertThat(result.externalOrderId()).isEqualTo("B-9009");
        assertThat(result.customerEmail()).isEqualTo("b@example.com");
        assertThat(result.currency()).isEqualTo("SEK");
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).sku()).isEqualTo("SKU1");
        assertThat(result.items().get(0).qty()).isEqualTo(2);
        assertThat(result.items().get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("199.00"));
    }

    @Test
    void throwsOnMissingOrderRef() throws Exception {
        String json = """
                {
                  "buyer": {"email":"b@example.com"},
                  "lines": [],
                  "currencyCode": "SEK"
                }
                """;
        JsonNode body = objectMapper.readTree(json);

        assertThatThrownBy(() -> mapper.map(body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("orderRef");
    }

    @Test
    void throwsOnMissingBuyer() throws Exception {
        String json = """
                {
                  "orderRef": "B-9009",
                  "lines": [],
                  "currencyCode": "SEK"
                }
                """;
        JsonNode body = objectMapper.readTree(json);

        assertThatThrownBy(() -> mapper.map(body))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("buyer");
    }
}
