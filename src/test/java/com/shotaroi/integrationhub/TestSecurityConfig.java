package com.shotaroi.integrationhub;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        var admin = User.builder()
                .username("admin")
                .password("{noop}admin-secret")
                .roles("ADMIN")
                .build();
        var partner = User.builder()
                .username("partner")
                .password("{noop}partner")
                .roles("PARTNER")
                .build();
        return new InMemoryUserDetailsManager(admin, partner);
    }
}
