package ru.luttsev.jooq.testcontainers.integration;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import ru.luttsev.jooq.testcontainers.JooqTestcontainersPlugin;
import ru.luttsev.jooq.testcontainers.config.JooqTestcontainersExtension;
import ru.luttsev.jooq.testcontainers.dependencies.RuntimeDependencies;

public final class JooqCodegenIntegration {

    private static final String JOOQ_PLUGIN_ID = "org.jooq.jooq-codegen-gradle";
    private static final String JOOQ_CODEGEN = "jooqCodegen";
    private static final String JOOQ_EXTENSION = "jooq";
    private static final String JDBC_URL_PREFIX = "jdbc:jooq-testcontainers:postgresql:///codegen?";
    private static final Duration MINIMUM_TIMEOUT = Duration.ofSeconds(1);

    private final Project project;
    private final JooqTestcontainersExtension extension;

    public JooqCodegenIntegration(Project project, JooqTestcontainersExtension extension) {
        this.project = project;
        this.extension = extension;
    }

    public void register() {
        project.getPluginManager().withPlugin(JOOQ_PLUGIN_ID, ignored -> configureJooqPlugin());
    }

    private void configureJooqPlugin() {
        addRuntimeDependencies();
        Object jooq = project.getExtensions().getByName(JOOQ_EXTENSION);
        project.getTasks().named(JOOQ_CODEGEN).configure(task -> configureTask(task, jooq));
    }

    private void addRuntimeDependencies() {
        Configuration runtime = project.getConfigurations().getByName(RuntimeDependencies.RUNTIME_CONFIGURATION);
        project.getConfigurations().getByName(JOOQ_CODEGEN).extendsFrom(runtime);
        project.getDependencies().add(JOOQ_CODEGEN, project.files(
                JooqTestcontainersPlugin.class.getProtectionDomain().getCodeSource().getLocation()));
    }

    private void configureTask(Task task, Object jooq) {
        JooqJdbcConfiguration.configure(jooq, jdbcUrl());
        CodegenInputs.register(task, extension);
    }

    private String jdbcUrl() {
        String image = extension.getPostgres().getImage().get();
        Duration timeout = extension.getPostgres().getStartupTimeout().get();
        validate(image, timeout);

        StringBuilder url = new StringBuilder(JDBC_URL_PREFIX
                + "image=" + URLEncoder.encode(image, StandardCharsets.UTF_8)
                + "&startupTimeoutSeconds=" + Math.max(1, timeout.toSeconds())
                + "&logLevel=" + extension.getLogging().getLevel().get().name()
                + "&loggingEnabled=" + extension.getLogging().getEnabled().get());
        appendLiquibaseOptions(url);
        return url.toString();
    }

    private void appendLiquibaseOptions(StringBuilder url) {
        appendOption(url, "changeLog", extension.getLiquibase().getChangeLog().get());
        appendOption(url, "contexts", extension.getLiquibase().getContexts().getOrElse(""));
        appendOption(url, "labels", extension.getLiquibase().getLabels().getOrElse(""));

        var searchPaths = extension.getLiquibase().getSearchPath().getFiles().stream()
                .map(File::getAbsolutePath)
                .toList();
        for (int index = 0; index < searchPaths.size(); index++) {
            appendOption(url, "searchPath." + index, searchPaths.get(index));
        }
        extension.getLiquibase().getParameters().get().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> appendOption(url, "parameter." + entry.getKey(), entry.getValue()));
    }

    private static void appendOption(StringBuilder url, String name, String value) {
        url.append('&')
                .append(URLEncoder.encode(name, StandardCharsets.UTF_8))
                .append('=')
                .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }

    private static void validate(String image, Duration timeout) {
        if (image.isBlank()) {
            throw new GradleException("PostgreSQL image must not be blank");
        }
        if (timeout.compareTo(MINIMUM_TIMEOUT) < 0 || timeout.getSeconds() > Integer.MAX_VALUE) {
            throw new GradleException("PostgreSQL startup timeout must be between 1 second and "
                    + Integer.MAX_VALUE + " seconds");
        }
    }
}
