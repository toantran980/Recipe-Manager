package com.example.recipemanager.service;

import com.example.recipemanager.controller.AuthController.TokenResponse;
import com.example.recipemanager.entity.User;

import com.example.recipemanager.repository.UserRepository;
import com.example.recipemanager.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public TokenResponse register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalStateException("Email already registered");
        }
        user.setUsername(user.getUsername());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmail(user.getEmail());
        user.setRoles(user.getRoles());
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail());
        return new TokenResponse(token);
    }

    public TokenResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new TokenResponse(token);
    }
}
