package com.shotaroi.integrationhub.orders.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderPayloadRepository extends JpaRepository<OrderPayloadEntity, UUID> {
}
