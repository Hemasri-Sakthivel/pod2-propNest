package com.cog.propNest.module.iam.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cog.propNest.module.iam.dto.AuthResponse;
import com.cog.propNest.module.iam.dto.LoginRequest;
import com.cog.propNest.module.iam.dto.RegisterRequest;
import com.cog.propNest.module.iam.entity.Role;
import com.cog.propNest.module.iam.entity.User;
import com.cog.propNest.module.iam.repository.RoleRepository;
import com.cog.propNest.module.iam.repository.UserRepository;
import com.cog.propNest.security.CustomUserDetailsService;
import com.cog.propNest.security.JwtService;

/**
 * Public authentication endpoints. {@code /register} creates a user with a BCrypt
 * password; {@code /login} verifies credentials and returns a JWT.
 */
@RestController
@RequestMapping("/propNest/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          AuthenticationManager authenticationManager,
                          CustomUserDetailsService userDetailsService,
                          JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    // POST - register
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterRequest dto) {
        if (isBlank(dto.getName()) || isBlank(dto.getEmail())
            || isBlank(dto.getPassword()) || dto.getRoleId() == null) {
            return message(400, "name, email, password and roleId are required");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            return message(409, "A user already exists with this email");
        }
        Optional<Role> role = roleRepository.findById(dto.getRoleId());
        if (role.isEmpty()) {
            return message(400, "Invalid roleId");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role.get());
        user.setStatus(User.Status.A);
        userRepository.save(user);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "User registered successfully");
        body.put("userId", user.getUserId());
        return ResponseEntity.status(201).body(body);
    }

    // POST - login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dto) {
        if (isBlank(dto.getEmail()) || isBlank(dto.getPassword())) {
            return message(400, "email and password are required");
        }
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword()));
        } catch (BadCredentialsException | DisabledException e) {
            return message(401, "Invalid email or password");
        }

        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow();
        String roleName = user.getRole() != null ? user.getRole().getRoleName() : "USER";

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getEmail());
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("role", roleName);

        String token = jwtService.generateToken(userDetails, claims);
        return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), roleName));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private ResponseEntity<Map<String, Object>> message(int status, String text) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", text);
        return ResponseEntity.status(status).body(body);
    }
}
