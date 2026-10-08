package io.github.yluttsev.jooq.testcontainers.integration;

import org.gradle.api.Task;
import org.gradle.api.tasks.PathSensitivity;
import io.github.yluttsev.jooq.testcontainers.config.JooqTestcontainersExtension;

final class CodegenInputs {

    private static final String IMAGE = "jooqTestcontainers.postgresImage";
    private static final String STARTUP_TIMEOUT = "jooqTestcontainers.startupTimeout";
    private static final String CHANGE_LOG = "jooqTestcontainers.changeLog";
    private static final String SEARCH_PATH = "jooqTestcontainers.searchPath";
    private static final String CONTEXTS = "jooqTestcontainers.contexts";
    private static final String LABELS = "jooqTestcontainers.labels";
    private static final String PARAMETERS = "jooqTestcontainers.parameters";

    private CodegenInputs() {
    }

    static void register(Task task, JooqTestcontainersExtension extension) {
        task.getInputs().property(IMAGE, extension.getPostgres().getImage());
        task.getInputs().property(STARTUP_TIMEOUT, extension.getPostgres().getStartupTimeout());
        task.getInputs().property(CHANGE_LOG, extension.getLiquibase().getChangeLog());
        task.getInputs().files(extension.getLiquibase().getSearchPath())
                .withPropertyName(SEARCH_PATH)
                .withPathSensitivity(PathSensitivity.RELATIVE);
        task.getInputs().property(CONTEXTS, extension.getLiquibase().getContexts().orElse(""));
        task.getInputs().property(LABELS, extension.getLiquibase().getLabels().orElse(""));
        task.getInputs().property(PARAMETERS, extension.getLiquibase().getParameters());
    }
}
