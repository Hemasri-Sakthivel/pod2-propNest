package com.cog.propNest.module.identityAccessManagement.security;

import com.cog.propNest.module.identityAccessManagement.exception.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;

/**
 * Validates the {@code Authorization: Bearer <accessToken>} header for every
 * protected IAM endpoint and binds the resulting {@link CurrentUser} to the
 * request via {@link AuthContext}.
 *
 * <p>Public endpoints (register, login, refresh-token) are skipped. Requests
 * outside the IAM module are not touched.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String IAM_PREFIX = "/IAM";

    /** Endpoints reachable without an access token. */
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/IAM/auth/register",
            "/IAM/auth/login",
            "/IAM/auth/refresh-token"
    );

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();

        // Anything outside the IAM module is left untouched.
        if (path == null || !path.startsWith(IAM_PREFIX) || PUBLIC_PATHS.contains(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractBearerToken(request);
        if (token == null) {
            writeUnauthorized(request, response, "Token missing or expired");
            return;
        }

        try {
            CurrentUser currentUser = jwtService.parseAccessToken(token);
            AuthContext.set(currentUser);
            filterChain.doFilter(request, response);
        } catch (InvalidTokenException ex) {
            writeUnauthorized(request, response, ex.getMessage());
        } finally {
            AuthContext.clear();
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                   String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Mirror the shape of common.response.ErrorResponse without depending on a
        // JSON library at the filter layer.
        String body = "{"
                + "\"status\":" + HttpStatus.UNAUTHORIZED.value() + ","
                + "\"message\":\"" + escape(message) + "\","
                + "\"path\":\"" + escape(request.getRequestURI()) + "\","
                + "\"timestamp\":\"" + Instant.now() + "\""
                + "}";
        response.getWriter().write(body);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
