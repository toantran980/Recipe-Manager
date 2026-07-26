package com.example.recipemanager.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis-backed token blacklist.
 * When a user logs out, their JWT is stored in Redis for the
 * remaining lifetime of the token so it cannot be reused.
 */
@Service
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * Add a token to the blacklist with a TTL matching its remaining validity.
     *
     * @param token      the JWT to blacklist
     * @param ttlSeconds how long the token should stay blacklisted (seconds)
     */
    public void blacklist(String token, long ttlSeconds) {
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", ttlSeconds, TimeUnit.SECONDS);
    }

    /**
     * Check whether a token has been blacklisted.
     *
     * @param token the JWT to check
     * @return true if the token is blacklisted
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}

