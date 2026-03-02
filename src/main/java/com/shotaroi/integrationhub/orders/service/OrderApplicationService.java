package com.shotaroi.integrationhub.orders.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shotaroi.integrationhub.orders.domain.CanonicalCreateOrderCommand;
import com.shotaroi.integrationhub.orders.domain.OrderStatus;
import com.shotaroi.integrationhub.orders.mapping.PartnerPayloadMapper;
import com.shotaroi.integrationhub.orders.mapping.PartnerPayloadMapperRegistry;
import com.shotaroi.integrationhub.orders.persistence.OrderEntity;
import com.shotaroi.integrationhub.orders.persistence.OrderPayloadEntity;
import com.shotaroi.integrationhub.orders.persistence.OrderPayloadRepository;
import com.shotaroi.integrationhub.orders.persistence.OrderRepository;
import com.shotaroi.integrationhub.outbox.domain.OutboxStatus;
import com.shotaroi.integrationhub.outbox.persistence.OutboxEventEntity;
import com.shotaroi.integrationhub.outbox.persistence.OutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderApplicationService {

    private final OrderRepository orderRepository;
    private final OrderPayloadRepository orderPayloadRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PartnerPayloadMapperRegistry mapperRegistry;
    private final ObjectMapper objectMapper;

    public OrderApplicationService(OrderRepository orderRepository,
                                   OrderPayloadRepository orderPayloadRepository,
                                   OutboxEventRepository outboxEventRepository,
                                   PartnerPayloadMapperRegistry mapperRegistry,
                                   ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.orderPayloadRepository = orderPayloadRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.mapperRegistry = mapperRegistry;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public Optional<OrderResult> findExisting(String partnerKey, String idempotencyKey) {
        return orderRepository.findByPartnerKeyAndIdempotencyKey(partnerKey, idempotencyKey)
                .map(o -> new OrderResult(o.getId(), o.getStatus()));
    }

    @Transactional
    public OrderResult createOrder(String partnerKey, String idempotencyKey, JsonNode body) {
        Optional<OrderEntity> existing = orderRepository.findByPartnerKeyAndIdempotencyKey(partnerKey, idempotencyKey);
        if (existing.isPresent()) {
            OrderEntity o = existing.get();
            return new OrderResult(o.getId(), o.getStatus());
        }

        PartnerPayloadMapper mapper = mapperRegistry.get(partnerKey);
        CanonicalCreateOrderCommand cmd = mapper.map(body);

        OrderEntity order = new OrderEntity(partnerKey, cmd.externalOrderId(), idempotencyKey, OrderStatus.RECEIVED);
        order = orderRepository.save(order);

        Map<String, Object> rawMap = objectMapper.convertValue(body, Map.class);
        Map<String, Object> normalizedMap = objectMapper.convertValue(cmd, Map.class);
        orderPayloadRepository.save(new OrderPayloadEntity(order, rawMap, normalizedMap));

        Map<String, Object> eventPayload = Map.of(
                "orderId", order.getId().toString(),
                "partnerKey", partnerKey,
                "externalOrderId", cmd.externalOrderId(),
                "status", OrderStatus.RECEIVED.name()
        );
        outboxEventRepository.save(new OutboxEventEntity(
                "ORDER", order.getId(), "ORDER_RECEIVED", eventPayload, OutboxStatus.PENDING
        ));

        return new OrderResult(order.getId(), order.getStatus());
    }

    @Transactional(readOnly = true)
    public Optional<OrderResult> getOrder(String partnerKey, UUID orderId) {
        return orderRepository.findByPartnerKeyAndId(partnerKey, orderId)
                .map(o -> new OrderResult(o.getId(), o.getStatus()));
    }

    public record OrderResult(UUID orderId, OrderStatus status) {}
}
