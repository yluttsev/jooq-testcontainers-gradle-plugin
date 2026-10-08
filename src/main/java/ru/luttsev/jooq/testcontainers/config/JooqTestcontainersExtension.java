package ru.luttsev.jooq.testcontainers.config;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import ru.luttsev.jooq.testcontainers.dependencies.RuntimeDependencies;

/**
 * Settings exposed through the {@code jooqTestcontainers} Gradle extension.
 * jOOQ generator settings remain in the official {@code jooq} extension.
 */
public class JooqTestcontainersExtension {

    private final PostgresSettings postgres;
    private final LiquibaseSettings liquibase;
    private final LoggingSettings logging;
    private final RuntimeDependencies dependencies;

    /**
     * Creates the extension with Gradle-managed settings objects.
     *
     * @param objects factory for nested settings
     * @param dependencies runtime dependency declarations
     */
    @Inject
    public JooqTestcontainersExtension(ObjectFactory objects, RuntimeDependencies dependencies) {
        this.postgres = objects.newInstance(PostgresSettings.class);
        this.liquibase = objects.newInstance(LiquibaseSettings.class);
        this.logging = objects.newInstance(LoggingSettings.class);
        this.dependencies = dependencies;
    }

    /**
     * Provides settings for the temporary PostgreSQL container.
     *
     * @return PostgreSQL container settings
     */
    public PostgresSettings getPostgres() {
        return postgres;
    }

    /**
     * Provides settings for migrations run before code generation.
     *
     * @return Liquibase migration settings
     */
    public LiquibaseSettings getLiquibase() {
        return liquibase;
    }

    /**
     * Provides settings for messages emitted by this plugin.
     *
     * @return plugin logging settings
     */
    public LoggingSettings getLogging() {
        return logging;
    }

    /**
     * Provides dependency declarations used while preparing the database.
     *
     * @return runtime dependency declarations
     */
    public RuntimeDependencies getDependencies() {
        return dependencies;
    }

    /**
     * Configures the temporary PostgreSQL container.
     *
     * @param action configuration action
     */
    public void postgres(Action<? super PostgresSettings> action) {
        action.execute(postgres);
    }

    /**
     * Configures the Liquibase changelog and its execution.
     *
     * @param action configuration action
     */
    public void liquibase(Action<? super LiquibaseSettings> action) {
        action.execute(liquibase);
    }

    /**
     * Configures messages emitted by this plugin.
     *
     * @param action configuration action
     */
    public void logging(Action<? super LoggingSettings> action) {
        action.execute(logging);
    }

    /**
     * Configures dependencies used while preparing the database.
     *
     * @param action configuration action
     */
    public void dependencies(Action<? super RuntimeDependencies> action) {
        action.execute(dependencies);
    }
}
