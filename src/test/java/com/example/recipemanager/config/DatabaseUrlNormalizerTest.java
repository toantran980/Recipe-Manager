package com.example.recipemanager.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DatabaseUrlNormalizerTest {

    @Test
    void extractsCredentialsAndRemovesThemFromPostgresqlJdbcUrl() {
        DatabaseUrlNormalizer.NormalizedJdbcUrl normalized = DatabaseUrlNormalizer.normalize(
                "postgresql://user:pass@host:5432/recipe?sslmode=require"
        );

        assertEquals("jdbc:postgresql://host:5432/recipe?sslmode=require", normalized.jdbcUrl());
        assertEquals("user", normalized.username());
        assertEquals("pass", normalized.password());
    }

    @Test
    void extractsCredentialsFromAlreadyPrefixedJdbcUrl() {
        DatabaseUrlNormalizer.NormalizedJdbcUrl normalized = DatabaseUrlNormalizer.normalize(
                "jdbc:postgresql://user:pass@host:5432/recipe"
        );

        assertEquals("jdbc:postgresql://host:5432/recipe", normalized.jdbcUrl());
        assertEquals("user", normalized.username());
        assertEquals("pass", normalized.password());
    }

    @Test
    void normalizesLegacyPostgresUrls() {
        DatabaseUrlNormalizer.NormalizedJdbcUrl normalized = DatabaseUrlNormalizer.normalize(
                "postgres://user:pass@host:5432/recipe"
        );

        assertEquals("jdbc:postgresql://host:5432/recipe", normalized.jdbcUrl());
        assertEquals("user", normalized.username());
        assertEquals("pass", normalized.password());
    }

    @Test
    void preservesJdbcUrlsWithoutEmbeddedCredentials() {
        DatabaseUrlNormalizer.NormalizedJdbcUrl normalized = DatabaseUrlNormalizer.normalize(
                "jdbc:postgresql://localhost:5432/recipe"
        );

        assertEquals("jdbc:postgresql://localhost:5432/recipe", normalized.jdbcUrl());
        assertEquals(null, normalized.username());
        assertEquals(null, normalized.password());
    }

    @Test
    void decodesPercentEncodedCredentials() {
        DatabaseUrlNormalizer.NormalizedJdbcUrl normalized = DatabaseUrlNormalizer.normalize(
                "postgresql://user:p%40ss%3Aword@host:5432/recipe"
        );

        assertEquals("jdbc:postgresql://host:5432/recipe", normalized.jdbcUrl());
        assertEquals("user", normalized.username());
        assertEquals("p@ss:word", normalized.password());
    }
}
