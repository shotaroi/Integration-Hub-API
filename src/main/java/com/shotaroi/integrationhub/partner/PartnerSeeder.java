package com.shotaroi.integrationhub.partner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class PartnerSeeder implements ApplicationRunner {

    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;

    public PartnerSeeder(PartnerRepository partnerRepository, PasswordEncoder passwordEncoder) {
        this.partnerRepository = partnerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedPartner("partner-a", "partner-a-secret");
        seedPartner("partner-b", "partner-b-secret");
    }

    private void seedPartner(String partnerKey, String apiKey) {
        if (partnerRepository.findByPartnerKey(partnerKey).isEmpty()) {
            partnerRepository.save(new PartnerEntity(partnerKey, passwordEncoder.encode(apiKey)));
        }
    }
}
