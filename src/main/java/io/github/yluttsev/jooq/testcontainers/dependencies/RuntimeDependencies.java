package io.github.yluttsev.jooq.testcontainers.dependencies;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

/**
 * Dependencies used by the temporary database and Liquibase during code generation.
 * The consuming project resolves them through its own repositories.
 */
public class RuntimeDependencies {

    /** Resolvable classpath used by jOOQ code generation. */
    public static final String RUNTIME_CONFIGURATION = "jooqTestcontainersRuntimeClasspath";

    /** Configuration for additional migration libraries. */
    public static final String MIGRATION_CONFIGURATION = "jooqMigrationRuntime";

    /** Default Liquibase core dependency. */
    public static final String DEFAULT_LIQUIBASE = "org.liquibase:liquibase-core:4.33.0";

    /** Default PostgreSQL JDBC driver dependency. */
    public static final String DEFAULT_POSTGRES_DRIVER = "org.postgresql:postgresql:42.7.13";

    /** Default Testcontainers BOM dependency. */
    public static final String DEFAULT_TESTCONTAINERS_PLATFORM = "org.testcontainers:testcontainers-bom:2.0.5";
    private static final String LIQUIBASE_CONFIGURATION = "jooqTestcontainersLiquibase";
    private static final String POSTGRES_DRIVER_CONFIGURATION = "jooqTestcontainersPostgresDriver";
    private static final String PLATFORM_CONFIGURATION = "jooqTestcontainersPlatform";
    private static final String MODULES_CONFIGURATION = "jooqTestcontainersModules";

    private final DependencyHandler handler;
    private final Configuration liquibase;
    private final Configuration postgresDriver;
    private final Configuration testcontainersPlatform;

    /**
     * Creates the default runtime dependency configurations.
     *
     * @param configurations project configurations
     * @param handler project dependency handler
     * @param objects factory for Gradle attributes
     */
    public RuntimeDependencies(ConfigurationContainer configurations, DependencyHandler handler, ObjectFactory objects) {
        this.handler = handler;
        liquibase = scope(configurations, LIQUIBASE_CONFIGURATION);
        postgresDriver = scope(configurations, POSTGRES_DRIVER_CONFIGURATION);
        testcontainersPlatform = scope(configurations, PLATFORM_CONFIGURATION);
        Configuration migrations = scope(configurations, MIGRATION_CONFIGURATION);
        Configuration modules = scope(configurations, MODULES_CONFIGURATION);

        liquibase.defaultDependencies(dependencies -> dependencies.add(handler.create(DEFAULT_LIQUIBASE)));
        postgresDriver.defaultDependencies(dependencies -> dependencies.add(handler.create(DEFAULT_POSTGRES_DRIVER)));
        testcontainersPlatform.defaultDependencies(dependencies ->
                dependencies.add(handler.platform(DEFAULT_TESTCONTAINERS_PLATFORM))
        );
        handler.add(modules.getName(), "org.testcontainers:testcontainers");
        handler.add(modules.getName(), "org.testcontainers:testcontainers-postgresql");

        Configuration runtime = configurations.create(RUNTIME_CONFIGURATION);
        runtime.setCanBeConsumed(false);
        runtime.setCanBeResolved(true);
        runtime.setVisible(false);
        runtime.setDescription("Dependencies used to prepare PostgreSQL for jOOQ code generation.");
        runtime.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.class, Usage.JAVA_RUNTIME));
        runtime.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.class, Category.LIBRARY));
        runtime.extendsFrom(liquibase, postgresDriver, testcontainersPlatform, migrations, modules);
    }

    /**
     * Replaces the default Liquibase core dependency.
     *
     * @param notation dependency notation or a provider of dependency notation
     */
    public void liquibase(Object notation) {
        replace(liquibase, notation);
    }

    /**
     * Replaces the default PostgreSQL JDBC driver dependency.
     *
     * @param notation dependency notation or a provider of dependency notation
     */
    public void postgresDriver(Object notation) {
        replace(postgresDriver, notation);
    }

    /**
     * Replaces the default Testcontainers BOM.
     *
     * @param notation platform dependency notation or a provider of dependency notation
     */
    public void testcontainersPlatform(Object notation) {
        Object platform = notation instanceof Provider<?> provider
                ? provider.map(handler::platform) : handler.platform(notation);
        replace(testcontainersPlatform, platform);
    }

    /**
     * Adds a library needed by migrations without replacing the default dependencies.
     * Repeated calls add multiple libraries.
     *
     * @param notation dependency notation or a provider of dependency notation
     */
    public void migrationRuntime(Object notation) {
        handler.add(MIGRATION_CONFIGURATION, notation);
    }

    private void replace(Configuration configuration, Object notation) {
        configuration.getDependencies().clear();
        handler.add(configuration.getName(), notation);
    }

    private static Configuration scope(ConfigurationContainer configurations, String name) {
        Configuration configuration = configurations.create(name);
        configuration.setCanBeConsumed(false);
        configuration.setCanBeResolved(false);
        configuration.setVisible(false);
        return configuration;
    }
}
