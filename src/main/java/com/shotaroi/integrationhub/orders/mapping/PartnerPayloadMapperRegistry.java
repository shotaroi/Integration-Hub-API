package com.shotaroi.integrationhub.orders.mapping;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class PartnerPayloadMapperRegistry {

    private final Map<String, PartnerPayloadMapper> mappers = Map.of(
            "partner-a", new PartnerAMapper(),
            "partner-b", new PartnerBMapper()
    );

    public PartnerPayloadMapper get(String partnerKey) {
        PartnerPayloadMapper mapper = mappers.get(partnerKey);
        if (mapper == null) {
            throw new IllegalArgumentException("Unknown partner: " + partnerKey);
        }
        return mapper;
    }
}
