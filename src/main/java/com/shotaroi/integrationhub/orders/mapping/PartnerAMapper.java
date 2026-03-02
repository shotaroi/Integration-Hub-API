package com.shotaroi.integrationhub.orders.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.shotaroi.integrationhub.orders.domain.CanonicalCreateOrderCommand;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PartnerAMapper implements PartnerPayloadMapper {

    @Override
    public CanonicalCreateOrderCommand map(JsonNode body) {
        String partnerOrderId = requireText(body, "partnerOrderId");
        String customerEmail = requireText(body, "customerEmail");
        String currency = requireText(body, "currency");
        List<CanonicalCreateOrderCommand.OrderItem> items = new ArrayList<>();

        JsonNode itemsNode = body.get("items");
        if (itemsNode == null || !itemsNode.isArray()) {
            throw new IllegalArgumentException("Missing or invalid 'items' array");
        }
        for (JsonNode item : itemsNode) {
            items.add(new CanonicalCreateOrderCommand.OrderItem(
                    requireText(item, "sku"),
                    requireInt(item, "qty"),
                    requireDecimal(item, "unitPrice")
            ));
        }
        return new CanonicalCreateOrderCommand(partnerOrderId, customerEmail, items, currency);
    }

    private static String requireText(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || !n.isTextual()) {
            throw new IllegalArgumentException("Missing or invalid field: " + field);
        }
        return n.asText();
    }

    private static int requireInt(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || !n.isNumber()) {
            throw new IllegalArgumentException("Missing or invalid field: " + field);
        }
        return n.asInt();
    }

    private static BigDecimal requireDecimal(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || !n.isNumber()) {
            throw new IllegalArgumentException("Missing or invalid field: " + field);
        }
        return n.decimalValue();
    }
}
