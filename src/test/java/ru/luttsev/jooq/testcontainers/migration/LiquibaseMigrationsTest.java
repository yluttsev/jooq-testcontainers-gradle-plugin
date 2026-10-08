package ru.luttsev.jooq.testcontainers.migration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.luttsev.jooq.testcontainers.support.PostgresContainers.running;
import static ru.luttsev.jooq.testcontainers.support.TestResources.copyResource;

@EnabledIfEnvironmentVariable(named = "JOOQ_TC_DOCKER_TEST", matches = "true")
class LiquibaseMigrationsTest {

    private static final String FIXTURES = "/fixtures/liquibase-migrations/";
    private static final String CHANGE_LOG = "src/integration/resources/migrations/main.yaml";

    @TempDir
    Path project;

    @Test
    void appliesConfiguredChangeLogBeforeCodeGeneration() throws Exception {
        Set<String> before = running();
        writeProject();

        run("jooqCodegen");

        assertTrue(generated("ConfiguredSample.java"));
        assertTrue(generated("SharedSample.java"));
        assertFalse(generated("SkippedSample.java"));
        assertEquals(before, running());
    }

    @Test
    void rerunsCodeGenerationWhenChangeLogChanges() throws Exception {
        writeProject();
        run("jooqCodegen");
        assertTrue(run("jooqCodegen").getOutput().contains("UP-TO-DATE"));

        Path changeLog = project.resolve(CHANGE_LOG);
        Files.writeString(changeLog, Files.readString(changeLog) + System.lineSeparator());

        assertFalse(run("jooqCodegen").getOutput().contains("UP-TO-DATE"));
    }

    @Test
    void stopsContainerWhenMigrationFails() throws Exception {
        Set<String> before = running();
        writeProject();
        Path build = project.resolve("build.gradle.kts");
        Files.writeString(build, Files.readString(build)
                .replace("migrations/main.yaml", "migrations/missing.yaml"));

        assertTrue(runFailure("jooqCodegen").getOutput().contains("migrations/missing.yaml"));

        assertEquals(before, running());
    }

    private void writeProject() throws IOException {
        copyResource("/fixtures/jooq-integration/settings.gradle.kts", project.resolve("settings.gradle.kts"));
        copyResource(FIXTURES + "build.gradle.kts", project.resolve("build.gradle.kts"));
        copyResource(FIXTURES + "migrations/main.yaml", project.resolve(CHANGE_LOG));
        copyResource(FIXTURES + "shared.yaml", project.resolve("src/integration/shared/shared.yaml"));
    }

    private boolean generated(String fileName) throws IOException {
        try (var files = Files.walk(project.resolve("build/generated-src/jooq"))) {
            return files.anyMatch(file -> file.getFileName().toString().equals(fileName));
        }
    }

    private BuildResult run(String... arguments) {
        return runner(arguments).build();
    }

    private BuildResult runFailure(String... arguments) {
        return runner(arguments).buildAndFail();
    }

    private GradleRunner runner(String... arguments) {
        return GradleRunner.create()
                .withProjectDir(project.toFile())
                .withPluginClasspath()
                .withArguments(arguments);
    }
}
