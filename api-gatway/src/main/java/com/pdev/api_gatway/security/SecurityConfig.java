package com.pdev.api_gatway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Gateway-level security configuration using Spring WebFlux Security.
 *
 * <p><b>Strategy:</b>
 * <ul>
 *   <li>Gateway enforces <b>coarse-grained</b> route-level authentication (all protected routes require a valid JWT).</li>
 *   <li>Public endpoints (registration, health) are explicitly permitted.</li>
 *   <li>JWT validation is done <b>offline</b> using Keycloak's JWKS endpoint — no roundtrip to Keycloak per request.</li>
 *   <li>The {@link KeycloakRoleConverter} maps Keycloak realm roles to Spring Security authorities.</li>
 * </ul>
 *
 * <p><b>CORS:</b> configured to allow Angular frontend on {@code localhost:4200}.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final KeycloakRoleConverter keycloakRoleConverter;

    public SecurityConfig(KeycloakRoleConverter keycloakRoleConverter) {
        this.keycloakRoleConverter = keycloakRoleConverter;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // ── CSRF: disabled — stateless JWT API, no session cookies ──────────
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // ── CORS: allow Angular dev server ──────────────────────────────────
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ── Authorization Rules ─────────────────────────────────────────────
                .authorizeExchange(auth -> auth

                        // ── Public endpoints (no token required) ─────────────────────
                        // Citizen self-registration is public — Keycloak handles login
                        .pathMatchers(HttpMethod.POST, "/api/citizens/register").permitAll()
                        // Actuator health/info for liveness probes
                        .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        // Allow pre-flight CORS OPTIONS requests
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ── Protected routes — any authenticated user (valid JWT) ─────
                        .pathMatchers("/api/citizens/**").authenticated()
                        .pathMatchers("/api/kyc/**").authenticated()
                        .pathMatchers("/api/documents/**").authenticated()
                        .pathMatchers("/api/certificates/**").authenticated()
                        .pathMatchers("/api/grievances/**").authenticated()
                        .pathMatchers("/api/notifications/**").authenticated()

                        // ── Everything else must be authenticated ────────────────────
                        .anyExchange().authenticated()
                )

                // ── OAuth2 Resource Server — validate Bearer JWT tokens ─────────────
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRoleConverter))
                )

                .build();
    }

    /**
     * CORS configuration allowing the Angular frontend on localhost:4200
     * to call the gateway with Authorization headers.
     */
    @Bean
    public org.springframework.web.cors.reactive.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration config = new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(java.util.List.of(
                "http://localhost:4200",   // Angular dev server
                "http://localhost:4201"    // Alternate Angular port
        ));
        config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(java.util.List.of("*"));
        config.setExposedHeaders(java.util.List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource source =
                new org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
