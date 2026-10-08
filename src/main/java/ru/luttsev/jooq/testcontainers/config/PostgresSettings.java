package ru.luttsev.jooq.testcontainers.config;

import org.gradle.api.provider.Property;

import java.time.Duration;

public abstract class PostgresSettings {

    public static final String DEFAULT_IMAGE = "postgres:18";
    public static final Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofSeconds(60);

    public PostgresSettings() {
        getImage().convention(DEFAULT_IMAGE);
        getStartupTimeout().convention(DEFAULT_STARTUP_TIMEOUT);
    }

    public abstract Property<String> getImage();

    public abstract Property<Duration> getStartupTimeout();
}
