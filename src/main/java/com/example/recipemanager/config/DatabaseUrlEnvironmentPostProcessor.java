package com.example.recipemanager.config;

import java.util.Map;

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

        String normalizedUrl = DatabaseUrlNormalizer.normalizeJdbcUrl(sourceUrl);
        if (normalizedUrl == null || normalizedUrl.equals(sourceUrl)) {
            return;
        }

        MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(new MapPropertySource(
                "database-url-normalizer",
                Map.of("spring.datasource.url", normalizedUrl)
        ));
    }
}
