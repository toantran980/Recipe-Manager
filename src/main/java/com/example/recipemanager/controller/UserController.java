package com.example.recipemanager.controller;

import com.example.recipemanager.dto.ChangePasswordRequest;
import com.example.recipemanager.dto.UpdateProfileRequest;
import com.example.recipemanager.entity.User;
import com.example.recipemanager.security.AuthUser;
import com.example.recipemanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User profile management endpoints")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "Get user profile")
    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(userService.getUserProfile(authUser.getUserId()));
    }

    @Operation(summary = "Update user profile")
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        User updated = userService.updateProfile(authUser.getUserId(), request.username(), request.email());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Change password")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        userService.changePassword(authUser.getUserId(), request.currentPassword(), request.newPassword());
        return ResponseEntity.ok("Password changed successfully");
    }
}