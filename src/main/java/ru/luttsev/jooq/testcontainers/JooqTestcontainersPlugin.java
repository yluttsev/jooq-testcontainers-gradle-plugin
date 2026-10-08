package ru.luttsev.jooq.testcontainers;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import ru.luttsev.jooq.testcontainers.config.JooqTestcontainersExtension;
import ru.luttsev.jooq.testcontainers.dependencies.RuntimeDependencies;
import ru.luttsev.jooq.testcontainers.integration.JooqCodegenIntegration;

public class JooqTestcontainersPlugin implements Plugin<Project> {

    private static final String EXTENSION_NAME = "jooqTestcontainers";

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
