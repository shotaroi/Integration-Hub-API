package com.shotaroi.integrationhub.orders.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.shotaroi.integrationhub.orders.domain.CanonicalCreateOrderCommand;

public interface PartnerPayloadMapper {

    CanonicalCreateOrderCommand map(JsonNode body);
}
