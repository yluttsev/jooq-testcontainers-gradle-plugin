package io.github.yluttsev.jooq.testcontainers;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import io.github.yluttsev.jooq.testcontainers.config.JooqTestcontainersExtension;
import io.github.yluttsev.jooq.testcontainers.dependencies.RuntimeDependencies;
import io.github.yluttsev.jooq.testcontainers.integration.JooqCodegenIntegration;

/**
 * Connects the official jOOQ code generation task to a temporary PostgreSQL database.
 * The database is prepared with Liquibase migrations before code generation starts.
 */
public class JooqTestcontainersPlugin implements Plugin<Project> {

    private static final String EXTENSION_NAME = "jooqTestcontainers";

    /** Creates the plugin entry point used by Gradle. */
    public JooqTestcontainersPlugin() {
    }

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("java-base");
        RuntimeDependencies dependencies = new RuntimeDependencies(
                project.getConfigurations(),
                project.getDependencies(),
                project.getObjects()
        );
        JooqTestcontainersExtension extension = project
                .getExtensions()
                .create(EXTENSION_NAME, JooqTestcontainersExtension.class, dependencies);

        new JooqCodegenIntegration(project, extension).register();
    }
}
