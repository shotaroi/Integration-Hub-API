package com.shotaroi.integrationhub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class SecurityTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("integrationhub")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void missingApiKeyReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-PARTNER-ID", "partner-a")
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void wrongApiKeyReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .header("X-PARTNER-ID", "partner-a")
                        .header("X-API-KEY", "wrong-key")
                        .header("Idempotency-Key", "key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void partnerCannotAccessAdminReturns401WhenUsingApiKey() throws Exception {
        // API key auth does not apply to admin endpoints; Basic auth is required
        mockMvc.perform(get("/api/v1/admin/orders")
                        .header("X-PARTNER-ID", "partner-a")
                        .header("X-API-KEY", "partner-a-secret"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void partnerRoleCannotAccessAdminReturns403() throws Exception {
        // User with PARTNER role (via Basic auth) cannot access admin endpoints
        mockMvc.perform(get("/api/v1/admin/orders")
                        .with(httpBasic("partner", "partner")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminWithBasicAuthCanAccessAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/orders")
                        .with(httpBasic("admin", "admin-secret")))
                .andExpect(status().isOk());
    }
}
