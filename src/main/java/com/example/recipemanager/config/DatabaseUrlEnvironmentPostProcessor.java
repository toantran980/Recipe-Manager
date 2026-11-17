package com.example.recipemanager.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String sourceUrl = environment.getProperty("spring.datasource.url");
        if (sourceUrl == null || sourceUrl.isBlank()) {
            sourceUrl = environment.getProperty("DATABASE_URL");
        }

        DatabaseUrlNormalizer.NormalizedJdbcUrl normalized = DatabaseUrlNormalizer.normalize(sourceUrl);
        Map<String, Object> properties = new HashMap<>();
        if (!Objects.equals(normalized.jdbcUrl(), sourceUrl)) {
            properties.put("spring.datasource.url", normalized.jdbcUrl());
        }
        if (normalized.username() != null && isMissing(environment.getProperty("SPRING_DATASOURCE_USERNAME"))) {
            properties.put("spring.datasource.username", normalized.username());
        }
        if (normalized.password() != null && isMissing(environment.getProperty("SPRING_DATASOURCE_PASSWORD"))) {
            properties.put("spring.datasource.password", normalized.password());
        }
        if (properties.isEmpty()) {
            return;
        }

        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(new MapPropertySource(
                "database-url-normalizer",
                properties
        ));
    }

    private boolean isMissing(String value) {
        return value == null || value.isBlank();
    }
}
