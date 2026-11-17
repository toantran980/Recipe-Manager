package com.example.recipemanager.config;

public final class DatabaseUrlNormalizer {

    private DatabaseUrlNormalizer() {
    }

    public static String normalizeJdbcUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return rawUrl;
        }

        String trimmedUrl = rawUrl.trim();
        if (trimmedUrl.startsWith("jdbc:")) {
            return trimmedUrl;
        }

        if (trimmedUrl.startsWith("postgresql://")) {
            return "jdbc:" + trimmedUrl;
        }

        if (trimmedUrl.startsWith("postgres://")) {
            return "jdbc:postgresql://" + trimmedUrl.substring("postgres://".length());
        }

        return trimmedUrl;
    }
}
