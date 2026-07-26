package com.example.recipemanager.controller;

import com.example.recipemanager.dto.LoginRequest;
import com.example.recipemanager.dto.RegisterRequest;
import com.example.recipemanager.entity.User;
import com.example.recipemanager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login, and logout endpoints")
public class AuthController {
    @Autowired
    private AuthService authService;

    // POST /api/auth/register
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(user));
    }

    // POST /api/auth/login
    @Operation(summary = "Log in an existing user")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        return ResponseEntity.ok(authService.login(loginRequest.email(), loginRequest.password(), request));
    }

    // POST /api/auth/logout
    @Operation(summary = "Log out and invalidate the current token")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }
        return ResponseEntity.noContent().build();
    }

    public record TokenResponse(String token) {}
}
