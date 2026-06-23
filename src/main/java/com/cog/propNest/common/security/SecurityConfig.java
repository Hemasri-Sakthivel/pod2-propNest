package com.cog.propNest.common.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Stateless, header-trust security for the Maintenance &amp; Facility service.
 *
 * <p>The API Gateway validates the JWT upstream and injects {@code X-User-Id} /
 * {@code X-User-Role}; this config only enforces role-based access on the existing
 * endpoints. No JWT library, secret, session, or IAM code lives here.
 *
 * <p>Role names match the gateway contract ({@code Tenant}, {@code MaintenanceTechnician},
 * {@code PropertyManager}, {@code PropNestAdmin}); {@link HeaderAuthFilter} adds the
 * {@code ROLE_} prefix so {@code hasAnyRole(...)} matches. {@code PropNestAdmin} is
 * granted on every endpoint (global override).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String BASE = "/propNest/maintenanceFacility";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ── MaintenanceRequest ──
                .requestMatchers(HttpMethod.POST, BASE + "/createRequest")
                    .hasAnyRole("Tenant", "PropNestAdmin")
                .requestMatchers(HttpMethod.GET,
                        BASE + "/fetchAllRequests",
                        BASE + "/fetchRequestById/**",
                        BASE + "/fetchRequestsByUnit/**",
                        BASE + "/fetchRequestsByTenant/**")
                    .hasAnyRole("Tenant", "MaintenanceTechnician", "PropertyManager", "PropNestAdmin")
                .requestMatchers(HttpMethod.PUT, BASE + "/assignTechnician/**")
                    .hasAnyRole("PropertyManager", "PropNestAdmin")
                .requestMatchers(HttpMethod.PUT, BASE + "/updateStatus/**")
                    .hasAnyRole("MaintenanceTechnician", "PropertyManager", "PropNestAdmin")
                .requestMatchers(HttpMethod.PUT, BASE + "/closeRequest/**")
                    .hasAnyRole("PropertyManager", "PropNestAdmin")
                .requestMatchers(HttpMethod.PUT, BASE + "/reopenRequest/**")
                    .hasAnyRole("Tenant", "PropertyManager", "PropNestAdmin")
                // ── WorkOrder ──
                .requestMatchers(HttpMethod.POST, BASE + "/createWorkOrder")
                    .hasAnyRole("PropertyManager", "PropNestAdmin")
                .requestMatchers(HttpMethod.GET,
                        BASE + "/fetchAllWorkOrders",
                        BASE + "/fetchWorkOrderById/**",
                        BASE + "/fetchWorkOrdersByTechnician/**")
                    .hasAnyRole("MaintenanceTechnician", "PropertyManager", "PropNestAdmin")
                .requestMatchers(HttpMethod.PUT,
                        BASE + "/updateWorkOrderStatus/**",
                        BASE + "/updateWorkOrderCosts/**",
                        BASE + "/completeWorkOrder/**")
                    .hasAnyRole("MaintenanceTechnician", "PropertyManager", "PropNestAdmin")
                .requestMatchers(HttpMethod.PUT, BASE + "/cancelWorkOrder/**")
                    .hasAnyRole("PropertyManager", "PropNestAdmin")
                // ── Everything else under the module requires any authenticated role ──
                .anyRequest().authenticated()
            )
            .addFilterBefore(new HeaderAuthFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(restAuthenticationEntryPoint())
                .accessDeniedHandler(restAccessDeniedHandler())
            );

        return http.build();
    }

    /** 401 when no/invalid identity headers are present. */
    private AuthenticationEntryPoint restAuthenticationEntryPoint() {
        return (request, response, authException) ->
                writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized: missing or invalid authentication headers",
                        request.getRequestURI());
    }

    /** 403 when the caller's role is not permitted for the endpoint. */
    private AccessDeniedHandler restAccessDeniedHandler() {
        return (request, response, accessDeniedException) ->
                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Forbidden: your role does not have access to this operation",
                        request.getRequestURI());
    }

    /** Writes a JSON body matching the shape of common.response.ErrorResponse. */
    private void writeError(HttpServletResponse response, int status, String message, String path)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String json = String.format(
                "{\"status\":%d,\"message\":\"%s\",\"path\":\"%s\",\"timestamp\":\"%s\"}",
                status, message, path, Instant.now().toString());
        response.getWriter().write(json);
    }
}
