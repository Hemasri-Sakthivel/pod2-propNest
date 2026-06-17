package com.cog.propNest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Exposes the BCrypt {@link PasswordEncoder} used for hashing and verifying
 * user passwords. We pull in {@code spring-security-crypto} only — full Spring
 * Security auto-configuration is intentionally not enabled; authentication is
 * handled by the IAM module's own JWT filter.
 */
@Configuration
public class SecurityBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
