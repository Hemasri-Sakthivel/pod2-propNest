package com.cog.propNest.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cog.propNest.module.iam.entity.User;
import com.cog.propNest.module.iam.repository.UserRepository;

/**
 * Loads users from the {@code users} table for Spring Security. Username = email,
 * password = stored BCrypt hash, authority = {@code ROLE_<roleName>}, and the
 * account is enabled only when status = A (Active).
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";
        boolean enabled = user.getStatus() == User.Status.A;

        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPasswordHash())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + roleName)))
            .disabled(!enabled)
            .build();
    }
}
