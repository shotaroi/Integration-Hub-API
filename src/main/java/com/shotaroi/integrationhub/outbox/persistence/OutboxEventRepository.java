package com.shotaroi.integrationhub.outbox.persistence;

import com.shotaroi.integrationhub.outbox.domain.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query("SELECT e FROM OutboxEventEntity e WHERE e.status IN :statuses " +
           "AND (e.nextAttemptAt IS NULL OR e.nextAttemptAt <= :now) ORDER BY e.createdAt")
    List<OutboxEventEntity> findPendingOrFailedReadyForRetry(List<OutboxStatus> statuses, Instant now);

    List<OutboxEventEntity> findByStatus(OutboxStatus status);

    List<OutboxEventEntity> findByAggregateId(UUID aggregateId);
}
