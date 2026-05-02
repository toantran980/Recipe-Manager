package com.example.recipemanager.controller;

import com.example.recipemanager.service.AuthService;
import com.example.recipemanager.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(user));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest.email(), loginRequest.password()));
    }

    public record LoginRequest(String email, String password) {}
    public record TokenResponse(String token) {}
}
