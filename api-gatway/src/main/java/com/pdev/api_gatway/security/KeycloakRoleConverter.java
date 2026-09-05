package com.pdev.api_gatway.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Converts Keycloak-issued JWTs into Spring Security
 * {@link AbstractAuthenticationToken}.
 *
 * <p>
 * Keycloak stores realm-level roles in the JWT under:
 * 
 * <pre>
 * {
 *   "realm_access": {
 *     "roles": ["ROLE_CITIZEN", "ROLE_OFFICER", "ROLE_ADMIN"]
 *   }
 * }
 * </pre>
 *
 * <p>
 * This converter extracts those roles and also merges any standard
 * OAuth2 scopes from the default {@link JwtGrantedAuthoritiesConverter}.
 */
@Component
public class KeycloakRoleConverter implements Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    /** Default converter that extracts OAuth2 scopes (e.g., SCOPE_openid) */
    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    /**
     * Extracts authorities from the JWT and wraps them in a
     * {@link JwtAuthenticationToken}.
     *
     * @param jwt the Keycloak-issued JWT
     * @return Mono emitting the authentication token with merged authorities
     */
    @Override
    public Mono<AbstractAuthenticationToken> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>(extractRealmRoles(jwt));

        // Also include standard OAuth2 scopes from the default converter
        Collection<GrantedAuthority> scopeAuthorities = defaultConverter.convert(jwt);
        if (scopeAuthorities != null) {
            authorities.addAll(scopeAuthorities);
        }

        return Mono.just(new JwtAuthenticationToken(jwt, authorities, jwt.getClaimAsString("preferred_username")));
    }

    /**
     * Extracts realm-level roles from the {@code realm_access.roles} claim.
     * Each role is prefixed with "ROLE_" to conform to Spring Security conventions.
     *
     * @param jwt the Keycloak JWT
     * @return list of granted authorities
     */
    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return List.of();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .filter(role -> !role.startsWith("default-roles") && !role.equals("offline_access")
                        && !role.equals("uma_authorization"))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toList());
    }
}
