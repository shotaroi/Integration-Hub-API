package com.shotaroi.integrationhub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

@Configuration
public class SecurityBeans {

    @Bean
    public PasswordEncoder passwordEncoder() {
        var bcrypt = new BCryptPasswordEncoder(10);
        return new DelegatingPasswordEncoder("bcrypt", Map.of(
                "bcrypt", bcrypt,
                "noop", org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance()
        ));
    }
}
