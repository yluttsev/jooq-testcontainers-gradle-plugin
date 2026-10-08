package ru.luttsev.jooq.testcontainers.config;

import org.gradle.api.provider.Property;
import org.slf4j.event.Level;

public abstract class LoggingSettings {

    public static final Level DEFAULT_LEVEL = Level.INFO;
    public static final boolean DEFAULT_ENABLED = true;

    public LoggingSettings() {
        getLevel().convention(DEFAULT_LEVEL);
        getEnabled().convention(DEFAULT_ENABLED);
    }

    public abstract Property<Level> getLevel();

    public abstract Property<Boolean> getEnabled();
}
