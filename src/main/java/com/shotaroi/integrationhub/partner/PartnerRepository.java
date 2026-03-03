package com.shotaroi.integrationhub.partner;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PartnerRepository extends JpaRepository<PartnerEntity, UUID> {

    Optional<PartnerEntity> findByPartnerKey(String partnerKey);
}
