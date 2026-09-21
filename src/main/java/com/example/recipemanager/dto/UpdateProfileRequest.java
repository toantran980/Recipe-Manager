package com.example.recipemanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 50, message = "Username must be 50 characters or fewer")
        String username,
        
        @Email(message = "Invalid email format")
        String email
) {
}