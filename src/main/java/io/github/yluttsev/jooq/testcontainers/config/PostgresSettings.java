package io.github.yluttsev.jooq.testcontainers.config;

import org.gradle.api.provider.Property;

import java.time.Duration;

/** Settings for the PostgreSQL Testcontainer used during code generation. */
public abstract class PostgresSettings {

    /** Default PostgreSQL image. */
    public static final String DEFAULT_IMAGE = "postgres:18";

    /** Default limit for container startup. */
    public static final Duration DEFAULT_STARTUP_TIMEOUT = Duration.ofSeconds(60);

    /** Applies the default image and startup timeout. */
    public PostgresSettings() {
        getImage().convention(DEFAULT_IMAGE);
        getStartupTimeout().convention(DEFAULT_STARTUP_TIMEOUT);
    }

    /**
     * Selects the PostgreSQL Docker image.
     *
     * @return Docker image, {@code postgres:18} by default
     */
    public abstract Property<String> getImage();

    /**
     * Limits how long the container may take to start.
     *
     * @return container startup timeout, 60 seconds by default
     */
    public abstract Property<Duration> getStartupTimeout();
}
