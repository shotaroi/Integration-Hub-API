package com.shotaroi.integrationhub.orders.persistence;

import com.shotaroi.integrationhub.orders.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    Optional<OrderEntity> findByPartnerKeyAndIdempotencyKey(String partnerKey, String idempotencyKey);

    Optional<OrderEntity> findByPartnerKeyAndId(String partnerKey, UUID id);

    List<OrderEntity> findByStatusIn(List<OrderStatus> statuses);
}
