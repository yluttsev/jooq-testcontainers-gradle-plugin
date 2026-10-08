package ru.luttsev.jooq.testcontainers.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.luttsev.jooq.testcontainers.support.TestResources.copyResource;

@EnabledIfEnvironmentVariable(named = "JOOQ_TC_DOCKER_TEST", matches = "true")
class CodegenInputsTest {

    private static final String FIXTURES = "/fixtures/liquibase-migrations/";
    private static final String CODEGEN_TASK = ":jooqCodegen";

    @TempDir
    Path project;

    @Test
    void rerunsWhenIncludedChangeLogChanges() throws IOException {
        writeProject();
        assertOutcome(TaskOutcome.SUCCESS);
        assertOutcome(TaskOutcome.UP_TO_DATE);

        Files.writeString(
                project.resolve("src/integration/shared/shared.yaml"),
                System.lineSeparator(),
                StandardOpenOption.APPEND
        );

        assertOutcome(TaskOutcome.SUCCESS);
    }

    @Test
    void rerunsWhenLiquibaseSettingsChange() throws IOException {
        writeProject();
        assertOutcome(TaskOutcome.SUCCESS);

        replaceBuild(
                "parameters.put(\"tableName\", \"configured_sample\")",
                "parameters.put(\"tableName\", \"renamed_sample\")"
        );
        assertOutcome(TaskOutcome.SUCCESS);
        assertTrue(generated("RenamedSample.java"));

        replaceBuild("contexts.set(\"development\")", "contexts.set(\"production\")");
        replaceBuild("labels.set(\"codegen\")", "labels.set(\"other\")");
        assertOutcome(TaskOutcome.SUCCESS);
        assertTrue(generated("SkippedSample.java"));
    }

    private void writeProject() throws IOException {
        copyResource("/fixtures/jooq-integration/settings.gradle.kts", project.resolve("settings.gradle.kts"));
        copyResource(FIXTURES + "build.gradle.kts", project.resolve("build.gradle.kts"));
        copyResource(FIXTURES + "migrations/main.yaml", project.resolve("src/integration/resources/migrations/main.yaml"));
        copyResource(FIXTURES + "shared.yaml", project.resolve("src/integration/shared/shared.yaml"));
    }

    private void replaceBuild(String previous, String replacement) throws IOException {
        Path build = project.resolve("build.gradle.kts");
        Files.writeString(build, Files.readString(build).replace(previous, replacement));
    }

    private boolean generated(String fileName) throws IOException {
        try (var files = Files.walk(project.resolve("build/generated-src/jooq"))) {
            return files.anyMatch(file -> file.getFileName().toString().equals(fileName));
        }
    }

    private void assertOutcome(TaskOutcome expected) {
        BuildResult result = GradleRunner.create()
                .withProjectDir(project.toFile())
                .withPluginClasspath()
                .withArguments("jooqCodegen")
                .build();
        assertEquals(expected, Objects.requireNonNull(result.task(CODEGEN_TASK)).getOutcome());
    }
}
