package ru.luttsev.jooq.testcontainers;

import org.gradle.api.provider.Property;

import java.time.Duration;

public abstract class PostgresSettings {

    public PostgresSettings() {
        getImage().convention("postgres:18");
        getStartupTimeout().convention(Duration.ofSeconds(60));
    }

    public abstract Property<String> getImage();
    public abstract Property<Duration> getStartupTimeout();
}
