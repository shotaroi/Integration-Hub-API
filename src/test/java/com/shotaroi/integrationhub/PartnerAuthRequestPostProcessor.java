package com.shotaroi.integrationhub;

import com.shotaroi.integrationhub.security.partner.PartnerPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

public final class PartnerAuthRequestPostProcessor {

    private PartnerAuthRequestPostProcessor() {}

    public static RequestPostProcessor partnerAuth(String partnerKey) {
        var principal = new PartnerPrincipal(
                partnerKey,
                List.of(new SimpleGrantedAuthority("ROLE_PARTNER"))
        );
        return SecurityMockMvcRequestPostProcessors.authentication(principal);
    }
}
