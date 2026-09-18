package com.example.recipemanager.security;

public class AuthUser {

    private final Long userId;
    private final String email;

    public AuthUser(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}