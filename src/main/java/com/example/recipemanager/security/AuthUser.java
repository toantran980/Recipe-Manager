package com.example.recipemanager.security;

public class AuthUser {

    private final String userId;
    private final String email;

    public AuthUser(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
