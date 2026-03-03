package com.shotaroi.integrationhub.partner;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final PasswordEncoder passwordEncoder;

    public PartnerService(PartnerRepository partnerRepository, PasswordEncoder passwordEncoder) {
        this.partnerRepository = partnerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean verify(String partnerKey, String apiKey) {
        return partnerRepository.findByPartnerKey(partnerKey)
                .map(p -> passwordEncoder.matches(apiKey, p.getApiKeyHash()))
                .orElse(false);
    }
}
