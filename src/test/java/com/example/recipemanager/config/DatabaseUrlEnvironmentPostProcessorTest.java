package com.example.recipemanager.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class DatabaseUrlEnvironmentPostProcessorTest {

    @Test
    void convertsNeonUrlAndUsesEmbeddedCredentialsAsDatasourceProperties() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty(
                        "spring.datasource.url",
                        "postgresql://neondb_owner:mypass@ep-example.neon.tech/neondb?sslmode=require"
                );

        new DatabaseUrlEnvironmentPostProcessor()
                .postProcessEnvironment(environment, new SpringApplication());

        assertEquals(
                "jdbc:postgresql://ep-example.neon.tech/neondb?sslmode=require",
                environment.getProperty("spring.datasource.url")
        );
        assertEquals("neondb_owner", environment.getProperty("spring.datasource.username"));
        assertEquals("mypass", environment.getProperty("spring.datasource.password"));
    }
}
