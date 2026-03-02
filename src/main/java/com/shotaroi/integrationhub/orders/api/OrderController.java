package com.shotaroi.integrationhub.orders.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.shotaroi.integrationhub.orders.service.OrderApplicationService;
import com.shotaroi.integrationhub.security.partner.PartnerPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Partner order ingestion API")
public class OrderController {

    private final OrderApplicationService orderService;

    public OrderController(OrderApplicationService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @Operation(summary = "Submit order", description = "Submit an order. Requires X-PARTNER-ID, X-API-KEY, Idempotency-Key headers.")
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @RequestBody JsonNode body,
            @AuthenticationPrincipal PartnerPrincipal principal
    ) {
        String partnerKey = principal != null ? principal.partnerKey() : null;
        if (partnerKey == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        OrderApplicationService.OrderResult result = orderService.createOrder(partnerKey, idempotencyKey.trim(), body);
        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new OrderResponse(result.orderId(), result.status().name()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order", description = "Get order by ID. Partner can only read own orders.")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal PartnerPrincipal principal
    ) {
        String partnerKey = principal != null ? principal.partnerKey() : null;
        if (partnerKey == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return orderService.getOrder(partnerKey, id)
                .map(r -> ResponseEntity.ok(new OrderResponse(r.orderId(), r.status().name())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record OrderResponse(UUID orderId, String status) {}
}
