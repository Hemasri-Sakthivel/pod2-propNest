package com.cog.propNest.security;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Stateless JWT security. {@code /propNest/auth/**} is public (register/login);
 * every other endpoint requires a valid Bearer token. 401/403 responses reuse the
 * existing {@link ErrorResponse} JSON shape.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtService jwtService,
                          CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtFilter =
            new JwtAuthenticationFilter(jwtService, userDetailsService);

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // public auth endpoints
                .requestMatchers("/propNest/auth/**").permitAll()

                // ── Tenant Application ──────────────────────────────
                .requestMatchers(HttpMethod.POST, "/propNest/tenantOnboarding/createTenant")
                    .hasAnyRole("TENANT", "PROPERTY_MANAGER", "REAL_ESTATE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/propNest/tenantOnboarding/getAllTenants",
                        "/propNest/tenantOnboarding/getTenantById/*")
                    .hasAnyRole("OWNER", "PROPERTY_MANAGER", "REAL_ESTATE_ADMIN", "TENANT")
                .requestMatchers(HttpMethod.PUT, "/propNest/tenantOnboarding/updateTenant/*")
                    .hasAnyRole("PROPERTY_MANAGER", "REAL_ESTATE_ADMIN")

                // ── Tenant KYC ──────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/propNest/tenantOnboarding/createKyc",
                        "/propNest/tenantOnboarding/uploadDocument")
                    .hasAnyRole("TENANT", "PROPERTY_MANAGER", "REAL_ESTATE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/propNest/tenantOnboarding/getAllKyc",
                        "/propNest/tenantOnboarding/getKycById/*",
                        "/propNest/tenantOnboarding/verifyDocument/*")
                    .hasAnyRole("PROPERTY_MANAGER", "REAL_ESTATE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/propNest/tenantOnboarding/verifyKyc/*")
                    .hasAnyRole("PROPERTY_MANAGER", "REAL_ESTATE_ADMIN")

                // ── Lease Agreement ─────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/propNest/tenantOnboarding/createLease")
                    .hasAnyRole("PROPERTY_MANAGER", "REAL_ESTATE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/propNest/tenantOnboarding/activateLease/*",
                        "/propNest/tenantOnboarding/renewLease/*",
                        "/propNest/tenantOnboarding/terminateLease/*")
                    .hasAnyRole("PROPERTY_MANAGER", "REAL_ESTATE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/propNest/tenantOnboarding/getAllLeases",
                        "/propNest/tenantOnboarding/getLeaseById/*")
                    .hasAnyRole("OWNER", "PROPERTY_MANAGER", "REAL_ESTATE_ADMIN", "FINANCE_EXECUTIVE")

                // everything else just needs a valid token
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler()))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> writeError(response,
            request.getRequestURI(), HttpStatus.UNAUTHORIZED,
            "Authentication required: missing or invalid token");
    }

    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> writeError(response,
            request.getRequestURI(), HttpStatus.FORBIDDEN, "Access denied");
    }

    private void writeError(HttpServletResponse response, String path,
                            HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        // Same shape as common.response.ErrorResponse (built manually to avoid a
        // hard Jackson dependency in the security layer).
        String json = String.format(
            "{\"status\":%d,\"message\":\"%s\",\"path\":\"%s\",\"timestamp\":\"%s\"}",
            status.value(), message, path, Instant.now());
        response.getWriter().write(json);
    }
}
