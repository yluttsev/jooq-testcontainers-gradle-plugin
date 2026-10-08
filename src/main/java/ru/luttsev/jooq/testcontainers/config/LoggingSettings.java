package ru.luttsev.jooq.testcontainers.config;

import org.gradle.api.provider.Property;
import org.slf4j.event.Level;

/** Controls messages emitted by this plugin without changing other tools' logging. */
public abstract class LoggingSettings {

    /** Default level for plugin messages. */
    public static final Level DEFAULT_LEVEL = Level.INFO;

    /** Whether plugin messages are enabled by default. */
    public static final boolean DEFAULT_ENABLED = true;

    /** Applies the default level and enabled flag. */
    public LoggingSettings() {
        getLevel().convention(DEFAULT_LEVEL);
        getEnabled().convention(DEFAULT_ENABLED);
    }

    /**
     * Sets the minimum level of messages from this plugin.
     *
     * @return minimum log level, {@link Level#INFO} by default
     */
    public abstract Property<Level> getLevel();

    /**
     * Turns this plugin's messages on or off.
     *
     * @return whether plugin messages are enabled, {@code true} by default
     */
    public abstract Property<Boolean> getEnabled();
}
