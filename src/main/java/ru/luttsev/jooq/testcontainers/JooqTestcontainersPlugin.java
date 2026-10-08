package ru.luttsev.jooq.testcontainers;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class JooqTestcontainersPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("java-base");
        RuntimeDependencies dependencies = new RuntimeDependencies(
                project.getConfigurations(), project.getDependencies(), project.getObjects());
        project.getExtensions().create("jooqTestcontainers", JooqTestcontainersExtension.class, dependencies);
    }
}
