package com.example.recipemanager.service;

import com.example.recipemanager.controller.AuthController.TokenResponse;
import com.example.recipemanager.entity.User;
import com.example.recipemanager.exception.EmailAlreadyExistsException;
import com.example.recipemanager.repository.UserRepository;
import com.example.recipemanager.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerReturnsTokenWhenUserIsValid() {
        User user = new User();
        user.setUsername("tester");
        user.setEmail("tester@example.com");
        user.setPassword("secret");

        when(userRepository.existsByEmail("tester@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId("user-1");
            return saved;
        });
        when(jwtService.generateToken("user-1", "tester@example.com")).thenReturn("jwt-token");

        TokenResponse response = authService.register(user);

        assertEquals("jwt-token", response.token());
        verify(userRepository).save(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        User user = new User();
        user.setUsername("tester");
        user.setEmail("tester@example.com");
        user.setPassword("secret");

        when(userRepository.existsByEmail("tester@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(user));
    }

    @Test
    void loginReturnsTokenWhenCredentialsMatch() {
        User user = new User();
        user.setId("user-1");
        user.setEmail("tester@example.com");
        user.setPassword("encoded-secret");

        when(userRepository.findByEmail("tester@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded-secret")).thenReturn(true);
        when(jwtService.generateToken("user-1", "tester@example.com")).thenReturn("jwt-token");

        TokenResponse response = authService.login("tester@example.com", "secret");

        assertEquals("jwt-token", response.token());
    }

    @Test
    void loginRejectsBadPassword() {
        User user = new User();
        user.setEmail("tester@example.com");
        user.setPassword("encoded-secret");

        when(userRepository.findByEmail("tester@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded-secret")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login("tester@example.com", "wrong"));
    }
}
