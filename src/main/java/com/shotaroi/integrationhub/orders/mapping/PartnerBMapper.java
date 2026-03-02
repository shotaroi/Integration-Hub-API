package com.shotaroi.integrationhub.orders.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.shotaroi.integrationhub.orders.domain.CanonicalCreateOrderCommand;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PartnerBMapper implements PartnerPayloadMapper {

    @Override
    public CanonicalCreateOrderCommand map(JsonNode body) {
        String orderRef = requireText(body, "orderRef");
        JsonNode buyer = body.get("buyer");
        if (buyer == null || !buyer.isObject()) {
            throw new IllegalArgumentException("Missing or invalid 'buyer' object");
        }
        String customerEmail = requireText(buyer, "email");
        String currency = requireText(body, "currencyCode");
        List<CanonicalCreateOrderCommand.OrderItem> items = new ArrayList<>();

        JsonNode linesNode = body.get("lines");
        if (linesNode == null || !linesNode.isArray()) {
            throw new IllegalArgumentException("Missing or invalid 'lines' array");
        }
        for (JsonNode line : linesNode) {
            items.add(new CanonicalCreateOrderCommand.OrderItem(
                    requireText(line, "productCode"),
                    requireInt(line, "amount"),
                    requireDecimal(line, "price")
            ));
        }
        return new CanonicalCreateOrderCommand(orderRef, customerEmail, items, currency);
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
