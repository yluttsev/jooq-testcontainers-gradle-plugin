package ru.luttsev.jooq.testcontainers;

import org.gradle.api.provider.Property;
import org.slf4j.event.Level;

public abstract class LoggingSettings {

    public LoggingSettings() {
        getLevel().convention(Level.INFO);
        getEnabled().convention(true);
    }

    public abstract Property<Level> getLevel();

    public abstract Property<Boolean> getEnabled();
}
