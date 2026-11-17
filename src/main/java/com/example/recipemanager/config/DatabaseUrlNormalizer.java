package com.example.recipemanager.config;

import java.net.URI;

public final class DatabaseUrlNormalizer {

    private DatabaseUrlNormalizer() {
    }

    public static NormalizedJdbcUrl normalize(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return new NormalizedJdbcUrl(rawUrl, null, null);
        }

        String trimmedUrl = rawUrl.trim();
        String uriText = trimmedUrl.startsWith("jdbc:")
                ? trimmedUrl.substring("jdbc:".length())
                : trimmedUrl;
        if (!uriText.startsWith("postgresql://") && !uriText.startsWith("postgres://")) {
            return new NormalizedJdbcUrl(trimmedUrl, null, null);
        }

        URI uri = URI.create(uriText);
        String userInfo = uri.getUserInfo();
        String username = null;
        String password = null;
        if (userInfo != null) {
            int separator = userInfo.indexOf(':');
            username = separator >= 0 ? userInfo.substring(0, separator) : userInfo;
            password = separator >= 0 ? userInfo.substring(separator + 1) : null;
        }

        String normalizedUri = uriText.startsWith("postgres://")
                ? "postgresql://" + uriText.substring("postgres://".length())
                : uriText;
        if (userInfo != null) {
            int authorityStart = normalizedUri.indexOf("://") + 3;
            int authorityEnd = normalizedUri.length();
            for (char delimiter : new char[] {'/', '?', '#'}) {
                int delimiterIndex = normalizedUri.indexOf(delimiter, authorityStart);
                if (delimiterIndex >= 0 && delimiterIndex < authorityEnd) {
                    authorityEnd = delimiterIndex;
                }
            }

            String authority = normalizedUri.substring(authorityStart, authorityEnd);
            int userInfoEnd = authority.lastIndexOf('@');
            normalizedUri = normalizedUri.substring(0, authorityStart)
                    + authority.substring(userInfoEnd + 1)
                    + normalizedUri.substring(authorityEnd);
        }

        return new NormalizedJdbcUrl("jdbc:" + normalizedUri, username, password);
    }

    public record NormalizedJdbcUrl(String jdbcUrl, String username, String password) {
    }
}
