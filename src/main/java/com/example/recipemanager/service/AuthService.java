package com.example.recipemanager.service;

import com.example.recipemanager.controller.AuthController.TokenResponse;
import com.example.recipemanager.entity.User;
import com.example.recipemanager.exception.EmailAlreadyExistsException;
import com.example.recipemanager.repository.UserRepository;
import com.example.recipemanager.security.JwtService;
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

    public TokenResponse login(String email, String password) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Email and password are required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new TokenResponse(token);
    }
}