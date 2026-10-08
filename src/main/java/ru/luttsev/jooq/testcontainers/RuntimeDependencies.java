package ru.luttsev.jooq.testcontainers;

import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

public class RuntimeDependencies {

    public static final String RUNTIME_CONFIGURATION = "jooqTestcontainersRuntimeClasspath";
    public static final String MIGRATION_CONFIGURATION = "jooqMigrationRuntime";
    public static final String DEFAULT_LIQUIBASE = "org.liquibase:liquibase-core:4.33.0";
    public static final String DEFAULT_POSTGRES_DRIVER = "org.postgresql:postgresql:42.7.13";
    public static final String DEFAULT_TESTCONTAINERS_PLATFORM = "org.testcontainers:testcontainers-bom:2.0.5";

    private final DependencyHandler handler;
    private final Configuration liquibase;
    private final Configuration postgresDriver;
    private final Configuration testcontainersPlatform;

    public RuntimeDependencies(ConfigurationContainer configurations, DependencyHandler handler, ObjectFactory objects) {
        this.handler = handler;
        liquibase = scope(configurations, "jooqTestcontainersLiquibase");
        postgresDriver = scope(configurations, "jooqTestcontainersPostgresDriver");
        testcontainersPlatform = scope(configurations, "jooqTestcontainersPlatform");
        Configuration migrations = scope(configurations, MIGRATION_CONFIGURATION);
        Configuration modules = scope(configurations, "jooqTestcontainersModules");

        liquibase.defaultDependencies(dependencies -> dependencies.add(handler.create(DEFAULT_LIQUIBASE)));
        postgresDriver.defaultDependencies(dependencies -> dependencies.add(handler.create(DEFAULT_POSTGRES_DRIVER)));
        testcontainersPlatform.defaultDependencies(dependencies ->
                dependencies.add(handler.platform(DEFAULT_TESTCONTAINERS_PLATFORM)));
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

    public void liquibase(Object notation) {
        replace(liquibase, notation);
    }

    public void postgresDriver(Object notation) {
        replace(postgresDriver, notation);
    }

    public void testcontainersPlatform(Object notation) {
        Object platform = notation instanceof Provider<?> provider
                ? provider.map(handler::platform) : handler.platform(notation);
        replace(testcontainersPlatform, platform);
    }

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
