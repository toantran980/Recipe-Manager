package com.example.recipemanager.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DatabaseUrlNormalizerTest {

    @Test
    void normalizesDriverlessPostgresqlUrls() {
        assertEquals(
                "jdbc:postgresql://user:pass@host:5432/recipe",
                DatabaseUrlNormalizer.normalizeJdbcUrl("postgresql://user:pass@host:5432/recipe")
        );
    }

    @Test
    void preservesJdbcUrls() {
        assertEquals(
                "jdbc:postgresql://localhost:5432/recipe",
                DatabaseUrlNormalizer.normalizeJdbcUrl("jdbc:postgresql://localhost:5432/recipe")
        );
    }

    @Test
    void normalizesLegacyPostgresUrls() {
        assertEquals(
                "jdbc:postgresql://user:pass@host:5432/recipe",
                DatabaseUrlNormalizer.normalizeJdbcUrl("postgres://user:pass@host:5432/recipe")
        );
    }
}
