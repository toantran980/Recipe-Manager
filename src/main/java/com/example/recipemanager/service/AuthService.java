package com.example.recipemanager.service;

import com.example.recipemanager.controller.AuthController.TokenResponse;
import com.example.recipemanager.entity.User;
import com.example.recipemanager.exception.EmailAlreadyExistsException;
import com.example.recipemanager.repository.UserRepository;
import com.example.recipemanager.security.JwtService;
import com.example.recipemanager.security.RateLimitFilter;
import com.example.recipemanager.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RateLimitFilter rateLimitFilter;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    public TokenResponse register(User user) {
        if (!StringUtils.hasText(user.getUsername()) ||
            !StringUtils.hasText(user.getEmail()) ||
            !StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("Username, email, and password are required");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail());
        return new TokenResponse(token);
    }

    public TokenResponse login(String email, String password, HttpServletRequest request) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Email and password are required");
        }

        String ip = getClientIp(request);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    rateLimitFilter.recordFailure(ip);
                    return new BadCredentialsException("Invalid credentials");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            rateLimitFilter.recordFailure(ip);
            throw new BadCredentialsException("Invalid credentials");
        }

        rateLimitFilter.reset(ip);
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new TokenResponse(token);
    }

    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            // Blacklist for 24 hours (86400s), covering the max token lifetime
            tokenBlacklistService.blacklist(token, 86400);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String ip = request.getRemoteAddr();
        return ip != null ? ip : "unknown";
    }
}
