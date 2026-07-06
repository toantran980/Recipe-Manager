package com.example.recipemanager.controller;

import com.example.recipemanager.dto.LoginRequest;
import com.example.recipemanager.dto.RegisterRequest;
import com.example.recipemanager.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerReturnsCreatedStatusForValidPayload() {
        AuthController.TokenResponse response = new AuthController.TokenResponse("token");
        when(authService.register(any())).thenReturn(response);

        ResponseEntity<AuthController.TokenResponse> result = authController.register(
                new RegisterRequest("alice", "alice@example.com", "password123")
        );

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("token", result.getBody().token());
    }

    @Test
    void loginReturnsOkStatusForValidPayload() {
        AuthController.TokenResponse response = new AuthController.TokenResponse("token");
        when(authService.login("alice@example.com", "password123")).thenReturn(response);

        ResponseEntity<AuthController.TokenResponse> result = authController.login(
                new LoginRequest("alice@example.com", "password123")
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("token", result.getBody().token());
    }
}
