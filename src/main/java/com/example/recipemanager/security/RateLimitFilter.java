package com.example.recipemanager.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Rate-limits login attempts per IP address using Redis.
 * After {@code maxAttempts} failures within the window,
 * further attempts are rejected with 429 Too Many Requests.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_PREFIX = "ratelimit:login:";

    @Value("${rate-limit.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${rate-limit.login.window-seconds:900}") // 15 minutes
    private int windowSeconds;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (isLoginRequest(request)) {
            String ip = getClientIp(request);
            String key = RATE_LIMIT_PREFIX + ip;

            String val = redisTemplate.opsForValue().get(key);
            int attempts = val != null ? Integer.parseInt(val) : 0;

            if (attempts >= maxAttempts) {
                response.setStatus(429);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"error\":\"Too many login attempts. Please try again later.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only apply to login endpoint
        return !isLoginRequest(request);
    }

    /**
     * Called by AuthService after a failed login attempt.
     */
    public void recordFailure(String ip) {
        String key = RATE_LIMIT_PREFIX + ip;
        redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
    }

    /**
     * Called by AuthService after a successful login attempt.
     */
    public void reset(String ip) {
        String key = RATE_LIMIT_PREFIX + ip;
        redisTemplate.delete(key);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return request.getServletPath().equals("/api/auth/login")
            && "POST".equalsIgnoreCase(request.getMethod());
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String ip = request.getRemoteAddr();
        return ip != null ? ip : "unknown";
    }
}

