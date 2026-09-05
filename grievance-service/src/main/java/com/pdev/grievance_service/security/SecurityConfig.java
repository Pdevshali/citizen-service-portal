package com.pdev.grievance_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Security configuration that builds an Authentication from headers forwarded by the API Gateway.
 * The gateway adds X-User-Id and X-User-Roles (comma-separated) after validating the JWT.
 * This avoids introducing a full resource-server inside each microservice while allowing
 * method-level @PreAuthorize checks to work based on roles.
 */
@Configuration
public class SecurityConfig {

    private static final String HEADER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_ID = "X-User-Id";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS));

        // Insert header auth filter before the UsernamePasswordAuthenticationFilter
        http.addFilterBefore(new HeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static class HeaderAuthenticationFilter extends OncePerRequestFilter {
        private static final Logger logger = LoggerFactory.getLogger(HeaderAuthenticationFilter.class);

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
           throws ServletException, IOException {
       String rolesHeader = request.getHeader(HEADER_ROLES);
       String userId = request.getHeader(HEADER_USER_ID);

          logger.debug("Incoming headers {}={}, {}={}", HEADER_USER_ID, userId, HEADER_ROLES, rolesHeader);

       List<SimpleGrantedAuthority> authorities = List.of();
       if (rolesHeader != null && !rolesHeader.isBlank()) {
           authorities = Arrays.stream(rolesHeader.split(","))
                   .map(String::trim)
                   .filter(s -> !s.isEmpty())
                   .map(r -> new SimpleGrantedAuthority("ROLE_" + r.toUpperCase()))
                   .collect(Collectors.toList());
       }

            if (userId != null && !userId.isBlank()) {
           UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                   userId, null, authorities);
           SecurityContextHolder.getContext().setAuthentication(auth);
       }
         filterChain.doFilter(request, response);
        }
    }
}
