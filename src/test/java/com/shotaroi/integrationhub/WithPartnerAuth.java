package com.shotaroi.integrationhub;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithPartnerAuth.WithPartnerAuthSecurityContextFactory.class)
public @interface WithPartnerAuth {

    String partnerKey() default "partner-a";

    class WithPartnerAuthSecurityContextFactory implements WithSecurityContextFactory<WithPartnerAuth> {
        @Override
        public SecurityContext createSecurityContext(WithPartnerAuth annotation) {
            var principal = new com.shotaroi.integrationhub.security.partner.PartnerPrincipal(
                    annotation.partnerKey(),
                    java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_PARTNER"))
            );
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(principal);
            return context;
        }
    }
}
