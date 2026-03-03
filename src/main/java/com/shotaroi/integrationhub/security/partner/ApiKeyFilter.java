package com.shotaroi.integrationhub.security.partner;

import com.shotaroi.integrationhub.partner.PartnerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String PARTNER_ID_HEADER = "X-PARTNER-ID";
    private static final String API_KEY_HEADER = "X-API-KEY";

    private final PartnerService partnerService;

    public ApiKeyFilter(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String partnerKey = request.getHeader(PARTNER_ID_HEADER);
        String apiKey = request.getHeader(API_KEY_HEADER);

        if (partnerKey == null || partnerKey.isBlank() || apiKey == null || apiKey.isBlank()) {
            sendUnauthorized(response, "Missing X-PARTNER-ID or X-API-KEY");
            return;
        }

        if (!partnerService.verify(partnerKey.trim(), apiKey)) {
            sendUnauthorized(response, "Invalid API key");
            return;
        }

        SecurityContextHolder.getContext().setAuthentication(
                new PartnerPrincipal(partnerKey.trim(), List.of(new SimpleGrantedAuthority("ROLE_PARTNER")))
        );
        filterChain.doFilter(request, response);
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/orders");
    }
}
