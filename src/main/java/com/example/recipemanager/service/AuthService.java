package com.example.recipemanager.service;

import com.example.recipemanager.controller.AuthController.TokenResponse;
import com.example.recipemanager.entity.User;
import com.example.recipemanager.exception.EmailAlreadyExistsException;
import com.example.recipemanager.repository.UserRepository;
import com.example.recipemanager.security.JwtService;
import com.example.recipemanager.security.RateLimitFilter;
import com.example.recipemanager.security.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
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
        log.info("User registration attempt for email: {}", user.getEmail());
        if (!StringUtils.hasText(user.getUsername()) ||
            !StringUtils.hasText(user.getEmail()) ||
            !StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("Username, email, and password are required");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            log.warn("Registration attempt with existing email: {}", user.getEmail());
            throw new EmailAlreadyExistsException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerified(false);
        String verificationToken = generateSecureToken();
        user.setVerificationToken(verificationToken);
        User savedUser = userRepository.save(user);

        // In production, send verification email
        log.info("Email verification token for {}: {}", user.getEmail(), verificationToken);

        String token = jwtService.generateToken(savedUser.getId().toString(), savedUser.getEmail());
        log.info("User registered successfully: {}", savedUser.getEmail());
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
        String token = jwtService.generateToken(user.getId().toString(), user.getEmail());
        return new TokenResponse(token);
    }

    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            // Blacklist for 24 hours (86400s), covering the max token lifetime
            tokenBlacklistService.blacklist(token, 86400);
        }
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        String resetToken = generateSecureToken();
        user.setResetToken(resetToken);
        user.setResetTokenExpiration(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        // In production, send email with reset link
        // For now, return the token in the response (development only)
        System.out.println("Password reset token for " + email + ": " + resetToken);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired token"));

        if (user.getResetTokenExpiration() == null || user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Token has expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiration(null);
        userRepository.save(user);
    }

    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new BadCredentialsException("Invalid verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    private String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        byte[] token = new byte[32];
        random.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
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
