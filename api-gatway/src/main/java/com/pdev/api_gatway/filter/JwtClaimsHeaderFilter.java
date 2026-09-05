package com.pdev.api_gatway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Global filter that runs on every request after JWT validation.
 *
 * <p>Extracts user identity claims from the validated JWT and forwards them
 * as custom HTTP headers to all downstream microservices. This allows downstream
 * services to identify the caller without re-validating the JWT token themselves.
 *
 * <p>Headers added:
 * <ul>
 *   <li>{@code X-User-Id}     — Keycloak user UUID (sub claim)</li>
 *   <li>{@code X-User-Email}  — User's email address</li>
 *   <li>{@code X-User-Name}   — Preferred username</li>
 *   <li>{@code X-User-Roles}  — Comma-separated list of roles</li>
 * </ul>
 */
@Component
public class JwtClaimsHeaderFilter implements GlobalFilter, Ordered {

    /** Runs early in the filter chain, after Spring Security authentication */
    @Override
    public int getOrder() {
        return -1;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    // Only process if the principal is a JWT token (authenticated user)
                    if (ctx.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                        Jwt jwt = (Jwt) jwtAuth.getPrincipal();

                        // Extract roles as a comma-separated string (strip "ROLE_" prefix for downstream)
                        String roles = jwtAuth.getAuthorities().stream()
                                .map(auth -> auth.getAuthority().replace("ROLE_", "").replace("SCOPE_", ""))
                                .filter(r -> !r.isBlank())
                                .collect(Collectors.joining(","));

                        // Mutate request to add user identity headers
                        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                .header("X-User-Id",    getClaimOrEmpty(jwt, "sub"))
                                .header("X-User-Email", getClaimOrEmpty(jwt, "email"))
                                .header("X-User-Name",  getClaimOrEmpty(jwt, "preferred_username"))
                                .header("X-User-Roles", roles)
                                .build();

                        return chain.filter(exchange.mutate().request(mutatedRequest).build());
                    }

                    // Not authenticated — just pass through (SecurityConfig will reject if needed)
                    return chain.filter(exchange);
                })
                // If no security context (e.g., public endpoints), just continue
                .switchIfEmpty(chain.filter(exchange));
    }

    /**
     * Safely retrieves a String claim from the JWT, returning empty string if absent.
     */
    private String getClaimOrEmpty(Jwt jwt, String claim) {
        String value = jwt.getClaimAsString(claim);
        return value != null ? value : "";
    }
}
