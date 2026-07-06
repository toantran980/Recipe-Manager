package com.example.recipemanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret:${JWT_SECRET:change-me-placeholder-32-chars-minimum-secret}}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:${JWT_EXPIRATION_MS:86400000}}")
    private long jwtExpirationMs;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public boolean isValidToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractEmail(String token) {
        return parseClaims(token).get("email", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
